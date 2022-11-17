<template>
  <div>
    <!-- 1. 자정 전에 시작해서 자정 이후 끝나는 일정이거나 -->
    <!-- 2. 이전 일정은 자정에 끝났는데 이동시간 때문에 일정 시작시간이 자정 이후이거나 -->
    <!-- 3. 딱 자정에 일정이 시작될 때 표시 -->
    <h5
      v-if="
        (this.calTime[this.timetable.orders - 1] < 1440 &&
          this.calTime[this.timetable.orders - 1] + this.timetable.costTime >
            1440) ||
        checkMidnight[this.timetable.orders - 1] ||
        this.calTime[this.timetable.orders - 1] == 1440
      "
    >
      0시 이후의 일정입니다.
    </h5>
    <v-card outlined color="#ffb4c2" class="my-4 ps-2" @click="showMarker">
      <v-card-title class="card-title">
        <div class="textCutting">
          {{ this.timetable.place }}
        </div>
        <v-icon @click.stop="delTimetable" v-show="memberOrAdmin == 2"
          >mdi-close</v-icon
        >
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row class="pa-0" style="justify-content: space-between">
            <v-col class="ma-0 pa-0" cols="11">
              <span>
                {{
                  Math.floor(this.calTime[this.timetable.orders - 1] / 60) > 23
                    ? (
                        Math.floor(
                          this.calTime[this.timetable.orders - 1] / 60
                        ) - 24
                      )
                        .toString()
                        .padStart(2, 0)
                    : Math.floor(this.calTime[this.timetable.orders - 1] / 60)
                        .toString()
                        .padStart(2, 0)
                }}
                :
                {{
                  (this.calTime[this.timetable.orders - 1] % 60)
                    .toString()
                    .padStart(2, 0)
                }}
                ~
                {{
                  Math.floor(
                    (this.calTime[this.timetable.orders - 1] +
                      this.timetable.costTime) /
                      60
                  ) > 23
                    ? (
                        Math.floor(
                          (this.calTime[this.timetable.orders - 1] +
                            this.timetable.costTime) /
                            60
                        ) - 24
                      )
                        .toString()
                        .padStart(2, 0)
                    : Math.floor(
                        (this.calTime[this.timetable.orders - 1] +
                          this.timetable.costTime) /
                          60
                      )
                        .toString()
                        .padStart(2, 0)
                }}
                :
                {{
                  (
                    (this.calTime[this.timetable.orders - 1] +
                      this.timetable.costTime) %
                    60
                  )
                    .toString()
                    .padStart(2, 0)
                }}
              </span>
              <span>
                (
                <span v-if="Math.floor(this.timetable.costTime / 60) > 0">
                  {{ Math.floor(this.timetable.costTime / 60) }}시간
                </span>
                <span v-if="this.timetable.costTime % 60 > 0">
                  {{ this.timetable.costTime % 60 }}분</span
                >
                )
              </span>
            </v-col>
            <v-col class="ma-0 pa-0" cols="1">
              <timetable-cost-time-modal
                :timetable="timetable"
                @changeCostTime="changeCostTime"
                class="ma-0 col-5 pa-2"
                v-if="memberOrAdmin == 2"
              />
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
    </v-card>

    <v-container fluid v-show="nextLat != 0">
      <v-row align="center" style="justify-content: space-between">
        <v-col cols="8" class="pa-0">
          <!-- color는 박스 테두리, item-color는 선택된 item. 색상코드 말고 material color로 표현해야 함 -->
          <v-select
            v-model="select"
            :items="selectList"
            item-text="text"
            item-value="value"
            label="Outlined style"
            return-object
            single-line
            :disabled="!searchClick"
            outlined
            dense
            hide-details
            color="#ff1744"
            item-color="red accent-3"
          ></v-select>
        </v-col>
        <v-col v-show="memberOrAdmin == 2" class="pa-0" cols="3"
          ><v-btn outlined class="TransitBtn" v-if="searchClick" @click="search"
            >검색</v-btn
          ><v-btn
            outlined
            class="TransitBtn"
            v-if="!searchClick"
            @click="searchClick = !searchClick"
            >수정</v-btn
          ></v-col
        >
      </v-row>
    </v-container>
    <div v-if="timetable.moveRoute" class="pt-2">
      <div v-for="(route, idx) in routeToJsonList" :key="idx + route">
        {{ route.text }}
        <span style="color: #ff1744">
          {{ route.duration }}
        </span>
      </div>
    </div>
    <h3 v-if="timetable.moveTime" style="text-align: center" class="pt-2">
      <span v-if="Math.floor(timetable.moveTime / 60) > 0"
        >{{ Math.floor(this.timetable.moveTime / 60) }} 시간</span
      >
      <span> {{ this.timetable.moveTime % 60 }} 분 </span>
    </h3>
  </div>
</template>

<script>
import TimetableCostTimeModal from "./TimetableCostTimeModal.vue";
import axios from "axios";
import { mapState, mapMutations } from "vuex";
const mapStore = "mapStore";

export default {
  name: "TimetableCard",
  components: {
    TimetableCostTimeModal,
  },
  data() {
    return {
      endTime: 0,

      valueEnum: {
        BUS: "transit",
        CAR: "driving",
        NONE: "NONE",
      },
      selectList: [
        { text: "선택안함", value: "NONE" },
        { text: "대중교통", value: "transit" },
        { text: "자동차", value: "driving" },
      ],
      select: { text: "선택안함", value: "NONE" },
      searchClick: false,

      //transit, moveRoute, moveTime 순서
      newData: undefined,

      newTransit: "",
      newMoveRoute: "",
      newMoveTime: 0,
    };
  },
  props: {
    timetable: Object,
    calTime: Array,
    checkMidnight: Array,
    memberOrAdmin: Number,

    //다음 Timetable의 Lat, Lng
    //plan의 tripdate
    nextLat: Number,
    nextLng: Number,
    tripDate: Array,
  },
  mounted() {
    //timetable의 transit에 따라 select 값을 변경..아마도
    this.selectList.forEach((select) => {
      if (select.value == this.valueEnum[this.timetable.transit]) {
        this.select = select;
      }
    });
  },
  watch: {
    //draggable로 순서 변경 시 select를 선택안함으로 변경
    timetableTransit() {
      this.selectList.forEach((select) => {
        if (select.value == this.valueEnum[this.timetable.transit]) {
          this.select = select;
        }
      });
    },
  },
  computed: {
    ...mapState(mapStore, ["spotInfo"]),
    //timetable의 moveRoute 쪼개기..?아마도
    routeList() {
      let list = this.timetable.moveRoute.split("/");
      let last = list[list.length - 2].split(" ");
      list.pop();
      list.pop();
      if (last[last.length - 1].includes("도보"))
        list.push("다음위치까지 " + last[last.length - 1]);
      else {
        let str = "";
        last.forEach((word) => {
          str += word + " ";
        });
        list.push(str);
      }
      return list;
    },
    routeToJsonList() {
      let array = [];
      this.routeList.forEach((route) => {
        let text = route.split(":");
        array.push({ text: text[0], duration: text[1] });
      });
      return array;
    },
    transitTime() {
      return (
        new Date(
          this.tripDate[0] +
            "-" +
            this.tripDate[1] +
            "-" +
            this.tripDate[2] +
            "."
        ).getTime() /
          1000 +
        this.calTime[this.timetable.orders - 1] * 60
      );
    },
    timetableTransit() {
      return this.timetable.transit;
    },
  },
  methods: {
    changeCostTime(newTimetable) {
      this.$emit("setTimetable", newTimetable);
    },
    delTimetable() {
      this.$emit("delTimetable", this.timetable.orders);
    },
    changeTransit() {
      if (this.newTransit != "BUS") {
        this.newMoveRoute = "";
      }
      let newTimetable = {
        orders: this.timetable.orders,
        place: this.timetable.place,
        lat: this.timetable.lat,
        lng: this.timetable.lng,
        costTime: this.timetable.costTime,
        moveTime: this.newData[2],
        transit: this.newData[0],
        moveRoute: this.newData[1],
      };
      this.$emit("setTimetable", newTimetable);
    },
    //경로검색
    async search() {
      this.searchClick = !this.searchClick;
      let transit = this.select.value;

      if (transit == "NONE") {
        this.newData = ["NONE", "", 0];
      } else if (transit == "transit") {
        await this.googleSearch(transit);
      } else {
        await this.naverSearch();
      }

      //transit 변경 웹소켓
      this.changeTransit();
      //calTime 재계산
    },
    async googleSearch(transit) {
      await axios
        .post(
          process.env.VUE_APP_API_URL + "/google",
          "https://maps.googleapis.com/maps/api/directions/json?origin=" +
            this.timetable.lat +
            "," +
            this.timetable.lng +
            "&destination=" +
            this.nextLat +
            "," +
            this.nextLng +
            "&mode=" +
            transit +
            "&departure_time=" +
            this.transitTime +
            "&key="
        )
        .then((res) => {
          if (!res.data.routes[0]) {
            return;
          }
          let data = res.data.routes[0].legs[0];
          let steps = data.steps;
          let move = "";
          //이동 시간 아마도
          let newMoveTime = 0;
          steps.forEach((step) => {
            newMoveTime += step.duration.value;
            if (step.transit_details) {
              let detail = step.transit_details;
              let sub = "";
              sub += detail.line.vehicle.name;
              if (detail.line.short_name) {
                sub += detail.line.short_name;
              } else {
                sub += detail.line.name;
              }
              sub += " ";
              sub +=
                detail.departure_stop.name +
                " - " +
                detail.arrival_stop.name +
                ":" +
                step.duration.text +
                "/";
              move += sub;
            } else {
              if (step.html_instructions) {
                move += step.html_instructions + ":" + step.duration.text + "/";
              } else {
                move += ":" + step.duration.text + "/";
              }
            }
          });

          newMoveTime = Math.ceil(newMoveTime / 60);
          let newMoveRoute = move;
          let newTransit = "BUS";

          let result = [newTransit, newMoveRoute, newMoveTime];
          this.newData = result;
          return result;
        });
    },
    async naverSearch() {
      await axios
        .post(
          process.env.VUE_APP_API_URL + "/naver",
          "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=" +
            this.timetable.lng +
            "," +
            this.timetable.lat +
            "&goal=" +
            this.nextLng +
            "," +
            this.nextLat
        )
        .then((res) => {
          let newMoveTime = Math.ceil(
            res.data.route.traoptimal[0].summary.duration / 60000
          );
          let newMoveRoute = "";
          let newTransit = "CAR";

          let result = [newTransit, newMoveRoute, newMoveTime];
          this.newData = result;
          return result;
        });
    },
    ...mapMutations(mapStore, ["SET_SPOT_INFO"]),
    showMarker() {
      this.SET_SPOT_INFO(null);
      this.SET_SPOT_INFO({
        place: this.timetable.place,
        lat: this.timetable.lat,
        lng: this.timetable.lng,
      });
    },
  },
};
</script>

<style scoped>
.card-title {
  justify-content: space-between;
  padding-bottom: 5px;
}
.textCutting {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 80%;
}
.TransitBtn {
  color: #ff1744;
}

h5 {
  display: flex;
  flex-direction: row;
  color: #ff1744;
  margin-top: 8px;
}

h5:before,
h5:after {
  content: "";
  flex: 1 1;
  border-bottom: 2px solid #ff1744;
  margin: auto;
}

.v-application .primary--text {
  color: red !important;
}
</style>
