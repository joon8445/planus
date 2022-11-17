<template>
  <div>
    <div>
      <v-container style="justify-content: center">
        <v-row class="pa-0 ma-0">
          <v-col cols="3" class="ma-0 pa-0 mr-3" style="align-self: center">
            <v-text-field
              class="centered-input"
              outlined
              v-model="startHour"
              type="number"
              :disabled="!startBtnClick"
              hide-details
              :min="0"
              :max="11"
              color="#ff1744"
            ></v-text-field>
          </v-col>
          <v-col cols="1" class="ma-0 pa-0" style="align-self: center"
            ><h3>시</h3></v-col
          >
          <v-spacer />
          <v-col cols="3" class="ma-0 pa-0 mr-3">
            <v-text-field
              class="centered-input"
              :disabled="!startBtnClick"
              outlined
              v-model="startMin"
              type="number"
              hide-details
              :min="0"
              :max="59"
              color="#ff1744"
            ></v-text-field>
          </v-col>
          <v-col cols="1" class="ma-0 pa-0" style="align-self: center"
            ><h3>분</h3></v-col
          >
          <v-spacer />
          <v-col cols="1" class="ma-0 pa-0 ml-2" style="align-self: center">
            <v-icon
              v-if="!startBtnClick"
              @click="startBtn"
              v-show="memberOrAdmin == 2"
              >mdi-pencil</v-icon
            >
            <v-icon
              v-else
              @click="changeStartTime(plan, startHour, startMin)"
              :disabled="!timeFlag"
              color="#ff1744"
              >mdi-check</v-icon
            >
          </v-col>
        </v-row>
      </v-container>
    </div>
    <draggable
      :list="timetableList"
      :disabled="!enabled"
      @start="dragging = true"
      @end="onEnd"
    >
      <timetable-card
        v-for="timetable in timetableList"
        :key="`tt` + timetable.orders"
        :nextLat="
          timetableList[timetable.orders]
            ? timetableList[timetable.orders].lat
            : 0
        "
        :nextLng="
          timetableList[timetable.orders]
            ? timetableList[timetable.orders].lng
            : 0
        "
        :tripDate="plan.tripDate"
        :timetable="timetable"
        :calTime="calTime"
        :startTime="startTime"
        :checkMidnight="checkMidnight"
        :memberOrAdmin="memberOrAdmin"
        @changeCalTime="changeCalTime"
        @setTimetable="setTimetable"
        @delTimetable="delTimetable"
      >
      </timetable-card>
    </draggable>
  </div>
</template>

<script>
import API from "@/api/RESTAPI";
import TimetableCard from "@/components/plans/TimetableCard.vue";
import draggable from "vuedraggable";

const api = API;

export default {
  components: {
    TimetableCard,
    draggable,
  },
  data() {
    return {
      timetableList: [],
      startTime: 0,
      calTime: [],
      checkMidnight: [],

      startHour: 0,
      startMin: 0,

      //드래그용
      enabled: true,
      dragging: false,

      startBtnClick: false,
    };
  },
  props: {
    tripId: Number,
    plan: Object,
    WebSocketStartTime: Object,
    deletedTimetableList: Object,
    addedTimetable: Object,
    setOrdersTimetableList: Object,
    changedTimetable: Object,
    memberOrAdmin: Number,
  },
  async created() {
    await this.getPlanList([this.plan.planId]);
    if (this.memberOrAdmin == 2) {
      this.enabled = true;
    } else {
      this.enabled = false;
    }
  },
  watch: {
    WebSocketStartTime(newVal) {
      if (newVal.planId == this.plan.planId) {
        this.startTime = newVal.startTime * 1;
        this.startHour = Math.floor(this.startTime / 60);
        this.startMin = this.startTime % 60;
      }
    },
    startTime() {
      this.calculateCalTime(this.calTime, this.timetableList, this.startTime);
      //re-rendering을 위해 배열 splice 필요
      this.calTime.splice(0, 1, this.startTime);
      this.checkMidnight.splice(0, 1, false);
    },
    timetableList: {
      handler() {
        this.calculateCalTime(this.calTime, this.timetableList, this.startTime);
        //re-rendering을 위해 배열 splice 필요
        this.calTime.splice(0, 1, this.startTime);
        this.checkMidnight.splice(0, 1, false);
        this.$emit("countTimetable", this.timetableList.length);
        this.$emit("changeTimetableList", this.timetableList);
      },
      deep: true,
    },
    deletedTimetableList(newVal) {
      if (this.plan.planId == newVal.planId) {
        this.timetableList = newVal.timetableList;
      }
    },
    setOrdersTimetableList(newVal) {
      if (this.plan.planId == newVal.planId) {
        this.timetableList = newVal.timetableList;
      }
      // //re-rendering을 위해 배열 splice
    },
    addedTimetable(newVal) {
      if (this.plan.planId == newVal.planId) {
        let newTimetable = {
          timetableId: newVal.timetableId,
          orders: newVal.orders,
          place: newVal.place,
          lat: newVal.lat,
          lng: newVal.lng,
          costTime: newVal.costTime,
          moveTime: newVal.moveTime,
          transit: newVal.transit,
          moveRoute: newVal.moveRoute,
        };
        this.timetableList.push(newTimetable);
        this.updateOrders(this.timetableList);
      }
    },
    changedTimetable(newVal) {
      if (this.plan.planId == newVal.planId) {
        //re-rendering을 위해 배열 splice
        this.timetableList.splice(newVal.orders - 1, 1, newVal);
      }
    },
    memberOrAdmin() {
      if (this.memberOrAdmin == 2) {
        this.enabled = true;
      } else {
        this.enabled = false;
      }
    },
  },
  computed: {
    timeFlag() {
      if (
        String(this.startHour).length > 2 ||
        String(this.startMin).length > 2
      ) {
        return false;
      }
      if (this.startHour == 0 && this.startMin == 0) {
        return false;
      }
      if (
        !Number.isInteger(Number(this.startHour)) ||
        !Number.isInteger(Number(this.startMin))
      ) {
        return false;
      }
      if (!this.startHour && !this.startMin) {
        return false;
      }
      if (this.startHour < 0) {
        return false;
      }
      if (this.startMin < 0 || this.startMin > 59) {
        return false;
      }
      if (this.startHour === "" || this.startMin === "") {
        return false;
      }
      return true;
    },
  },
  methods: {
    async getPlanList(planIdList) {
      api.getPlanList(planIdList).then((res) => {
        this.timetableList = res.planList[0].timetableList;
        this.startTime = res.planList[0].startTime;
        // this.calTime = this.startTime;
        this.startHour = Math.floor(this.startTime / 60);
        this.startMin = this.startTime % 60;

        //timetableList 정렬
        this.sortTimetableList(this.timetableList);

        //calTime 계산
        this.calculateCalTime(this.calTime, this.timetableList, this.startTime);
      });
    },
    changeCalTime(endTime) {
      this.calTime = endTime;
    },
    sortTimetableList(timetableList) {
      timetableList.sort(function (a, b) {
        return a.orders - b.orders;
      });
    },
    calculateCalTime(calTime, timetableList, startTime) {
      for (let i = 0; i < timetableList.length; i++) {
        if (i == 0) {
          calTime[0] = startTime;
        } else {
          calTime[i] =
            calTime[i - 1] +
            timetableList[i - 1].costTime +
            timetableList[i - 1].moveTime;
          //1440을 넘는데 이전 값이 1440 이전 -> true값 주기
          if (
            calTime[i] > 1440 &&
            calTime[i - 1] + timetableList[i - 1].costTime < 1440
          ) {
            this.checkMidnight[i] = true;
          }
        }
      }
    },
    startBtn() {
      this.startBtnClick = !this.startBtnClick;
    },
    changeStartTime(plan, startHour, startMin) {
      this.startTime = parseInt(startHour) * 60 + parseInt(startMin);
      let newPlan = {
        planId: plan.planId,
        startTime: this.startTime,
        tripDate: plan.tripDate,
      };
      //emit으로 newPlan을 PlansView로 보내기
      this.$emit("setPlan", newPlan);
      this.startBtnClick = !this.startBtnClick;
    },
    setTimetable(newTimetable) {
      //emit으로 올라온 newTimetable의 costTime 값으로 timetable 값을 수정
      this.$emit("setTimetable", newTimetable, this.plan.planId);
      //해당 timetable 객체의 costTime을 수동으로 바꿔줌 .. ...왜..?
      this.timetableList[newTimetable.orders - 1].costTime =
        newTimetable.costTime;
    },
    delTimetable(delOrders) {
      //delOrder의 Timetable만 빼고 새로운 timetableList를 구성
      let delTimetableList = [];
      for (let i = 0; i < this.timetableList.length; i++) {
        if (this.timetableList[i].orders == delOrders) {
          continue;
        }
        let delTimetable = {
          place: this.timetableList[i].place,
          lat: this.timetableList[i].lat,
          lng: this.timetableList[i].lng,
          orders: this.timetableList[i].orders,
          costTime: this.timetableList[i].costTime,
          moveTime: this.timetableList[i].moveTime,
          transit: this.timetableList[i].transit,
          moveRoute: this.timetableList[i].moveRoute,
        };

        // orders가 delOrders-1이면 transit, moveTime, moveRoute를 초기화
        if (this.timetableList[i].orders == delOrders - 1) {
          delTimetable.transit = "NONE";
          delTimetable.moveTime = 0;
          delTimetable.moveRoute = "";
        }

        delTimetableList.push(delTimetable);
      }

      //delTimetableList를 돌면서 orders를 갱신
      this.updateOrders(delTimetableList);

      //emit으로 delTimetableList를 올려보내기
      this.$emit("delTimetable", this.plan.planId, delTimetableList);
    },
    // Timetable이 추가돼도 삭제돼도 orders가 제대로 안 들어가서.. orders 갱신하는 함수 따로 뺌
    updateOrders(list) {
      for (let i = 0; i < list.length; i++) {
        list[i].orders = i + 1;
      }
    },
    onEnd(event) {
      //oldIndex : 이동 전 인덱스
      //newIndex : 이동 후 인덱스

      let oldIdx = event.oldIndex;
      let newIdx = event.newIndex;

      if (oldIdx != newIdx) {
        this.changeDraggedOrders(oldIdx, newIdx);
      }

      this.dragging = false;
    },
    changeDraggedOrders(oldIdx, newIdx) {
      //기존 위치-1, 자기자신, 현재 위치-1의 transit, moveTime, moveRoute 초기화

      // 밑->위 : oldIdx만 변경
      // 위->밑 : oldIdx-1만 변경
      if (oldIdx > newIdx) {
        this.timetableList[oldIdx].transit = "NONE";
        this.timetableList[oldIdx].moveTime = 0;
        this.timetableList[oldIdx].moveRoute = "";
      } else {
        if (oldIdx != 0) {
          this.timetableList[oldIdx - 1].transit = "NONE";
          this.timetableList[oldIdx - 1].moveTime = 0;
          this.timetableList[oldIdx - 1].moveRoute = "";
        }
      }

      //splice로 oldIdx -> newIdx 배열 내 위치 변경

      this.timetableList[newIdx].transit = "NONE";
      this.timetableList[newIdx].moveTime = 0;
      this.timetableList[newIdx].moveRoute = "";

      if (newIdx != 0) {
        this.timetableList[newIdx - 1].transit = "NONE";
        this.timetableList[newIdx - 1].moveTime = 0;
        this.timetableList[newIdx - 1].moveRoute = "";
      }

      this.updateOrders(this.timetableList);

      //웹소켓 형식에 맞게 재구성
      let setTimetableOrdersList = [];
      for (let i = 0; i < this.timetableList.length; i++) {
        let timetable = {
          place: this.timetableList[i].place,
          lat: this.timetableList[i].lat,
          lng: this.timetableList[i].lng,
          orders: this.timetableList[i].orders,
          costTime: this.timetableList[i].costTime,
          moveTime: this.timetableList[i].moveTime,
          transit: this.timetableList[i].transit,
          moveRoute: this.timetableList[i].moveRoute,
        };
        setTimetableOrdersList.push(timetable);
      }

      this.$emit(
        "setTimetableOrders",
        this.plan.planId,
        setTimetableOrdersList
      );
    },
  },
};
</script>

<style></style>
