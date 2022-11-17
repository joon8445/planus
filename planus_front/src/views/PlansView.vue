<template>
  <div>
    <v-container
      d-flex
      class="ma-0 pa-0"
      style="align-items: center; max-width: 100%"
      v-if="isConnect"
    >
      <voice-chat
        :tripId="tripId"
        :nickname="nickname"
        class="ml-1"
        style="align-items: center"
      ></voice-chat>
      <v-spacer></v-spacer>
      <invite-dialog
        :tripId="tripId"
        :tripUrl="tripUrl"
        :admin="admin"
        :connector="connector"
        :userId="parseInt(userId)"
        @getConnector="getConnector"
        @changeAdmin="changeAdmin"
      ></invite-dialog>
      <complete-dialog
        :tripId="tripId"
        :tripUrl="tripUrl"
        :planIdList="planIdList"
        :memberOrAdmin="memberOrAdmin"
        @completeTrip="completeTrip"
        class="mr-3"
      ></complete-dialog>
    </v-container>
    <div>
      <v-tabs
        v-model="planTabs"
        fixed-tabs
        slider-color="#4a8072"
        color="#4a8072"
        show-arrows
      >
        <v-tab
          v-for="plan in planIdList"
          :key="plan.planId"
          @click="saveTimetableList(plan.planId)"
          >{{ plan.tripDate[1] }} / {{ plan.tripDate[2] }}</v-tab
        >
      </v-tabs>
    </div>
    <v-container d-flex class="ma-0 pt-0" style="max-width: 100%">
      <div style="height: 24px; width: 2%; min-width: 24px" v-if="isLeftFold">
        <v-icon class="chatButton" @click="leftFoldBtn"
          >mdi-chevron-double-right</v-icon
        >
      </div>

      <v-container
        class="ma-0 pt-0"
        style="width: 20%; min-width: 300px; height: 85vh; position: relative"
        v-show="!isLeftFold"
      >
        <div style="height: 24px">
          <v-icon class="chatButton" style="float: right" @click="leftFoldBtn"
            >mdi-chevron-double-left</v-icon
          >
        </div>
        <v-tabs
          v-model="tabs"
          fixed-tabs
          slider-color="#4a8072"
          color="#4a8072"
        >
          <v-tab style="padding: 0">버킷리스트</v-tab>
          <v-tab style="padding: 0">장소검색</v-tab>
          <v-tab style="padding: 0" @click="recommendClick">추천장소 </v-tab>
        </v-tabs>
        <v-tabs-items v-model="tabs">
          <v-tab-item>
            <bucket-list
              :tripId="tripId"
              @delBucket="delBucket"
              :deletedBucket="deletedBucket"
              :addedBucket="addedBucket"
              :memberOrAdmin="memberOrAdmin"
              @addTimetable="addTimetable"
              :addedTimetable="addedTimetable"
              @changeBucketList="changeBucketList"
              id="leftTab"
            ></bucket-list>
          </v-tab-item>
          <v-tab-item>
            <search-place-tab
              :mapLat="lat"
              :mapLng="lng"
              :size="size"
              :isRecommendClick="isRecommendClick"
              @addBucket="addBucket"
              @addTimetable="addTimetable"
              :memberOrAdmin="memberOrAdmin"
              id="leftTab"
            ></search-place-tab>
          </v-tab-item>
          <v-tab-item>
            <recommend-place-tab
              :mapLat="lat"
              :mapLng="lng"
              :size="size"
              :isRecommendClick="isRecommendClick"
              :memberOrAdmin="memberOrAdmin"
              @addBucket="addBucket"
              @addTimetable="addTimetable"
              @recommendClick="recommendClick"
              id="leftTab"
            ></recommend-place-tab>
          </v-tab-item>
        </v-tabs-items>
        <chat-tab
          :chatList="chatList"
          @sendMessage="sendChat"
          style="
            position: absolute;
            left: 0;
            bottom: 0;
            background-color: #4a8072;
          "
        />
      </v-container>
      <plan-map
        :style="planMapWidth"
        style="background-color: blue"
        :tripArea="tripArea"
        @getCenter="getCenter"
        :bucketList="bucketList"
        :timetableList="timetableList"
      />
      <v-container
        class="ma-0"
        style="width: 20%; min-width: 300px; height: 85vh"
      >
        <v-tabs-items v-model="planTabs">
          <v-tab-item v-for="plan in planIdList" :key="`plan` + plan.planId">
            <plan-list
              class="rightTab"
              :plan="plan"
              :tripId="tripId"
              :WebSocketStartTime="startTime"
              :deletedTimetableList="deletedTimetableList"
              :setOrdersTimetableList="setOrdersTimetableList"
              :addedTimetable="addedTimetable"
              :changedTimetable="changedTimetable"
              :memberOrAdmin="memberOrAdmin"
              @setPlan="setPlan"
              @setTimetable="setTimetable"
              @countTimetable="countTimetable"
              @delTimetable="delTimetable"
              @setTimetableOrders="setTimetableOrders"
              @changeTimetableList="changeTimetableList"
            ></plan-list>
          </v-tab-item>
        </v-tabs-items>
      </v-container>
    </v-container>
  </div>
</template>

<script>
import API from "@/api/RESTAPI";
import WSAPI from "@/api/WSAPI";
import RecommendPlaceTab from "@/components/recommend/RecommendPlaceTab.vue";
import PlanMap from "@/components/plans/PlanMap.vue";
import InviteDialog from "@/components/manageTrip/InviteDialog.vue";
import BucketList from "@/components/bucketList/BucketList.vue";
import jwt_decode from "jwt-decode";
import ChatTab from "@/components/chat/ChatTab.vue";
import PlanList from "@/components/plans/PlanList.vue";
import CompleteDialog from "@/components/manageTrip/CompleteDialog.vue";
import VoiceChat from "@/components/chat/VoiceChat.vue";
import SearchPlaceTab from "@/components/searchPlace/SearchPlaceTab.vue";

const ws = WSAPI;
const api = API;
export default {
  name: "PlanView",
  components: {
    PlanMap,
    InviteDialog,
    RecommendPlaceTab,
    BucketList,
    ChatTab,
    PlanList,
    CompleteDialog,
    VoiceChat,
    SearchPlaceTab,
  },
  data() {
    return {
      isConnect: false,
      tabs: null,
      dialog: false,
      tripId: 0,
      tripUrl: "",
      admin: 0,
      memberOrAdmin: 0,
      tripArea: [],
      lat: 37.5168415735,
      lng: 127.0341090296,
      size: 5,
      token: "Bearer " + this.$cookies.get("token"),
      nickname: "",
      userId: 0,
      chatList: [],
      connector: [],
      isRecommendClick: false,
      deletedBucket: {},
      addedBucket: {},
      addedTimetable: {},
      //삭제된 것을 제외한 timetableList
      deletedTimetableList: {},
      //순서 변경 완료된 timetableList
      setOrdersTimetableList: {},
      //변경된 timetable
      changedTimetable: {},

      planIdList: [],
      planTabs: null,

      startTime: {},
      timeTableLength: 0,

      planId: 0,

      bucketList: [],
      timetableList: [],
      tempAddBucket: {},
      isLeftFold: false,
      planMapWidth: "width: 60%",
    };
  },
  async created() {
    this.tripUrl = this.$route.params.tripUrl;
    await this.getTripInfo();
    await this.getPlanId(this.tripId);
    this.decoding();
    this.lat = this.tripArea[0].lat;
    this.lng = this.tripArea[0].lng;

    this.res = await api.getTimetableListForMap(this.planId);
    this.timetableList = this.res.timetableList;
  },
  watch: {
    async planId(newVal, oldVal) {
      if (oldVal == 0) {
        return;
      }
      //탭 변경이므로 false
      let complete = false;
      await api.savePlan(this.tripId, [oldVal], complete);
      //PlanMap timetableList 변경 api로 mysql의 timetableList를 가져오는 것
      this.res = await api.getTimetableListForMap(newVal);
      this.timetableList = this.res.timetableList;
    },
  },
  methods: {
    leftFoldBtn() {
      if (!this.isLeftFold) this.planMapWidth = "width: 78%";
      else this.planMapWidth = "width: 60%";
      this.isLeftFold = !this.isLeftFold;
    },
    async getTripInfo() {
      let data = await api.getTripInfo(this.tripUrl).catch(() => {
        window.alert("존재하지 않는 url입니다!");
        this.$router.push("/");
      });
      let result = data.result;
      this.tripId = result.tripId;
      this.admin = result.admin;
      this.memberOrAdmin = result.memberOrAdmin;
      this.tripArea = result.tripArea;
      if (result.complete) {
        this.$router.push("/complete/" + this.tripUrl);
      } else {
        switch (this.memberOrAdmin) {
          case -1:
            window.alert(
              "일정짜기가 진행중인 경우 로그인한 사용자만 입장 가능합니다!"
            );
            this.$router.push("/");
            break;
          case 0:
            this.addMember();
            break;
          case 1:
            this.connect();
            break;
          case 2:
            this.connect();
            break;
        }
      }
    },

    async addMember() {
      this.res = await api.addMember(this.tripId);
      if (this.res.memberId == -2) {
        window.alert("이미 참가자로 등록되어있습니다!");
        this.$router.push("/");
      } else if (this.res.memberId == -1) {
        window.alert("정원 10명이 마감되어 참가할 수 없습니다!");
        this.$router.push("/");
      } else {
        this.connect();
      }
    },
    connect() {
      this.isConnect = true;
      ws.connect(this.tripId, this.token, this.onSocketReceive);
    },
    async onSocketReceive(result) {
      const content = JSON.parse(result.body);
      switch (content.action) {
        case 0:
          this.connector = content.connector;
          break;
        case 1:
          this.chatList.push(content.userName + ": " + content.chatMsg);
          break;
        case 2:
          // TODO: 버킷리스트 추가
          this.addedBucket = {
            place: content.place,
            address: content.address,
            lat: content.lat,
            lng: content.lng,
          };
          break;
        case 3:
          // TODO: 버킷리스트 삭제
          this.deletedBucket = {
            place: content.place,
            address: content.address,
            lat: content.lat,
            lng: content.lng,
          };
          break;
        case 4:
          // TODO: 일정(plan)변경
          this.startTime = {
            planId: content.planId,
            startTime: content.startTime,
          };
          break;
        case 5:
          // TODO: 일정(timetable)추가
          this.addedTimetable = {
            costTime: content.costTime,
            place: content.place,
            lat: content.lat,
            lng: content.lng,
            orders: content.orders,
            fromBucket: content.fromBucket,
            address: content.address,
            planId: content.planId,
            transit: content.transit,
            moveTime: content.moveTime,
            moveRoute: content.moveRoute,
          };
          break;
        case 6:
          // TODO: 일정(timetable)삭제
          this.deletedTimetableList = {
            planId: content.planId,
            timetableList: content.timetableList,
          };
          break;
        case 7:
          // TODO: 일정(timetable) 순서 변경
          this.setOrdersTimetableList = {
            planId: content.planId,
            timetableList: content.timetableList,
          };
          break;
        case 8:
          // TODO: 일정(timetable) 수정
          this.changedTimetable = {
            costTime: content.costTime,
            place: content.place,
            lat: content.lat,
            lng: content.lng,
            orders: content.orders,
            transit: content.transit,
            moveTime: content.moveTime,
            moveRoute: content.moveRoute,
            planId: content.planId,
          };
          break;
        case 9:
          this.admin = content.newAdminId;
          if (content.newAdminId == this.userId) {
            this.memberOrAdmin = 2;
          } else {
            this.memberOrAdmin = 1;
          }
          break;
        case 10:
          // 완료페이지로 이동
          if (content.completed) {
            this.$router.push("/complete/" + this.tripUrl);
          }
          break;
      }
    },
    getConnector() {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.getConnector({
            tripId: this.tripId,
            token: this.token,
          });
        }
      }
    },
    sendChat(message) {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.chat({
            userName: this.nickname,
            chatMsg: message,
            tripId: this.tripId,
          });
        }
      }
    },
    addBucket(place, address, lat, lng) {
      this.tempAddBucket = {
        place: place,
        address: address,
        lat: lat,
        lng: lng,
      };
      if (this.token && this.checkDuplicateBucket()) {
        if (ws.stomp && ws.stomp.connected) {
          ws.addBucket(this.tripId, place, address, lat, lng);
        }
      }
    },
    addTimetable(costTime, place, lat, lng, fromBucket, address) {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.addTimetable(
            this.tripId,
            this.planId,
            costTime,
            place,
            lat,
            lng,
            this.timeTableLength + 1,
            fromBucket,
            address
          );
        }
      }
    },
    decoding() {
      let decode = jwt_decode(this.token);
      this.nickname = decode.nickname;
      this.userId = decode.userId;
    },
    getCenter(nowCenter) {
      this.lat = nowCenter.lat;
      this.lng = nowCenter.lng;
    },
    recommendClick() {
      this.isRecommendClick = !this.isRecommendClick;
    },
    delBucket(bucket) {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.delBucket(
            this.tripId,
            bucket.place,
            bucket.address,
            bucket.lat,
            bucket.lng
          );
        }
      }
    },
    async getPlanId(tripId) {
      await api.getPlanId(tripId).then((res) => {
        this.planIdList = res.planIdList;
        this.planId = res.planIdList[0].planId;
      });
    },
    setPlan(newPlan) {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.setPlan(
            this.tripId,
            newPlan.planId,
            newPlan.tripDate,
            newPlan.startTime
          );
        }
      }
    },
    setTimetable(newTimetable, planId) {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.setTimetable(
            this.tripId,
            planId,
            newTimetable.place,
            newTimetable.lat,
            newTimetable.lng,
            newTimetable.orders,
            newTimetable.costTime,
            newTimetable.moveTime,
            newTimetable.transit,
            newTimetable.moveRoute,
            false
          );
        }
      }
    },
    countTimetable(length) {
      this.timeTableLength = length;
    },
    async saveTimetableList(planId) {
      //planId에 변경사항이 생겼으므로 watch로
      //같은 탭을 누를 경우 planId가 동일하므로 변경사항이 생기지 않음
      this.planId = planId;
    },
    delTimetable(planId, delTimetableList) {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.delTimetable(this.tripId, planId, delTimetableList);
        }
      }
    },
    setTimetableOrders(planId, setTimetableOrdersList) {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.setTimetableOrders(this.tripId, planId, setTimetableOrdersList);
        }
      }
    },
    changeBucketList(bucketList) {
      this.bucketList = bucketList;
    },
    changeTimetableList(timtableList) {
      this.timetableList = timtableList;
    },
    changeAdmin(newAdminId) {
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.changeAdmin(this.tripId, newAdminId, this.admin);
        }
      }
    },
    checkDuplicateBucket() {
      for (const idx of this.bucketList.keys()) {
        if (
          JSON.stringify(this.bucketList[idx]) ==
          JSON.stringify(this.tempAddBucket)
        ) {
          window.alert("이미 추가한 장소입니다.");
          return false;
        }
      }
      return true;
    },
    completeTrip() {
      let completed = true;
      if (this.token) {
        if (ws.stomp && ws.stomp.connected) {
          ws.completeTrip(this.tripId, completed);
        }
      }
    },
  },
};
</script>

<style>
/* .v-slide-group__prev {
  display: none !important;
}
.v-slide-group__next {
  display: none !important;
} */
#leftTab {
  overflow-y: scroll;
  height: calc(85vh - 150px);
}
#leftTab::-webkit-scrollbar {
  color: "#00000000";
  width: 10px;
}
#leftTab::-webkit-scrollbar-thumb {
  background-color: #544c4c;
  border-radius: 10px;
  background-clip: padding-box;
  border: 2px solid transparent;
  border-color: #00000000;
}
.rightTab {
  overflow-y: scroll;
  height: 85vh;
}
.rightTab::-webkit-scrollbar {
  color: "#00000000";
  width: 10px;
}
.rightTab::-webkit-scrollbar-thumb {
  background-color: #544c4c;
  border-radius: 10px;
  background-clip: padding-box;
  border: 2px solid transparent;
  border-color: #00000000;
}
/* .v-tabs-slider {
  color: #4a8072;
}
.v-tab--active {
  color: #4a8072 !important;
} */
</style>
