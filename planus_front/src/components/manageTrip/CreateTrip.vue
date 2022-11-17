<template>
  <div class="createTripDiv">
    <div class="selectAreaMenu">
      <v-menu
        ref="areaMenu"
        v-model="areaMenu"
        :close-on-content-click="false"
        :return-value.sync="areas"
        transition="scale-transition"
        offset-y
        min-width="auto"
      >
        <template v-slot:activator="{ on, attrs }">
          <v-combobox
            v-model="areas"
            label="여행지 선택"
            allow-overflow
            multiple
            chips
            solo
            clearable
            readonly
            appendIcon=""
            clear-icon="mdi-close-circle"
            style="z-index: 1"
            color="#4a8072"
            v-bind="attrs"
            v-on="on"
            @click:clear="clearArea"
          >
          </v-combobox>
        </template>
        <v-card v-model="areaMenu" style="width: 700px">
          <v-card-title>
            <v-tabs
              v-model="tabs"
              fixed-tabs
              slider-color="#4a8072"
              color="#4a8072"
            >
              <v-tab style="padding: 0">서울/인천</v-tab>
              <v-tab>경기도</v-tab>
              <v-tab>강원도</v-tab>
              <v-tab>충청도</v-tab>
              <v-tab>경상도</v-tab>
              <v-tab>전라도</v-tab>
              <v-tab>제주도</v-tab>
            </v-tabs>
          </v-card-title>
          <v-card-text
            ><v-tabs-items v-model="tabs"
              ><v-tab-item>
                <v-chip-group column>
                  <v-chip
                    color="#4a8072"
                    text-color="white"
                    v-for="(area, i) in areaGroup1"
                    :key="i"
                    :area="area"
                    @click="addArea(area)"
                  >
                    {{ area.siName }}
                  </v-chip>
                </v-chip-group>
              </v-tab-item>
              <v-tab-item>
                <v-chip-group column>
                  <v-chip
                    color="#4a8072"
                    text-color="white"
                    v-for="(area, i) in areaGroup2"
                    :key="i"
                    :area="area"
                    @click="addArea(area)"
                  >
                    {{ area.siName }}
                  </v-chip>
                </v-chip-group>
              </v-tab-item>
              <v-tab-item>
                <v-chip-group column>
                  <v-chip
                    color="#4a8072"
                    text-color="white"
                    v-for="(area, i) in areaGroup3"
                    :key="i"
                    :area="area"
                    @click="addArea(area)"
                  >
                    {{ area.siName }}
                  </v-chip>
                </v-chip-group>
              </v-tab-item>
              <v-tab-item>
                <v-chip-group column>
                  <v-chip
                    color="#4a8072"
                    text-color="white"
                    v-for="(area, i) in areaGroup4"
                    :key="i"
                    :area="area"
                    @click="addArea(area)"
                  >
                    {{ area.siName }}
                  </v-chip>
                </v-chip-group>
              </v-tab-item>
              <v-tab-item>
                <v-chip-group column>
                  <v-chip
                    color="#4a8072"
                    text-color="white"
                    v-for="(area, i) in areaGroup5"
                    :key="i"
                    :area="area"
                    @click="addArea(area)"
                  >
                    {{ area.siName }}
                  </v-chip>
                </v-chip-group>
              </v-tab-item>
              <v-tab-item>
                <v-chip-group column>
                  <v-chip
                    color="#4a8072"
                    text-color="white"
                    v-for="(area, i) in areaGroup6"
                    :key="i"
                    :area="area"
                    @click="addArea(area)"
                  >
                    {{ area.siName }}
                  </v-chip>
                </v-chip-group>
              </v-tab-item>
              <v-tab-item>
                <v-chip-group column>
                  <v-chip
                    color="#4a8072"
                    text-color="white"
                    v-for="(area, i) in areaGroup7"
                    :key="i"
                    :area="area"
                    @click="addArea(area)"
                  >
                    {{ area.siName }}
                  </v-chip>
                </v-chip-group>
              </v-tab-item>
            </v-tabs-items>
          </v-card-text>
        </v-card>
      </v-menu>
    </div>
    <div class="selectDateMenu">
      <v-menu
        ref="dateMenu"
        v-model="dateMenu"
        :close-on-content-click="false"
        :return-value.sync="dates"
        transition="scale-transition"
        offset-y
        min-width="auto"
      >
        <template v-slot:activator="{ on, attrs }">
          <v-combobox
            v-model="dates"
            label="여행 일정 선택"
            multiple
            chips
            solo
            clearable
            readonly
            appendIcon=""
            prepend-inner-icon="mdi-calendar"
            clear-icon="mdi-close-circle"
            v-bind="attrs"
            v-on="on"
            color="#4a8072"
          >
          </v-combobox>
        </template>
        <v-date-picker
          v-model="dates"
          :allowed-dates="disablePastDates"
          range
          no-title
          scrollable
          locale="ko-KR"
          :day-format="getDay"
          color="#4a8072"
        >
          <v-spacer></v-spacer>
          <v-btn outlined color="#4a8072" @click="dateMenu = false">
            취소
          </v-btn>
          <v-btn outlined color="#4a8072" @click="$refs.dateMenu.save(dates)">
            선택
          </v-btn>
        </v-date-picker>
      </v-menu>
    </div>
    <v-btn class="createTripBtn" @click="createTrip">시작하기</v-btn>
  </div>
</template>

<script>
import API from "@/api/RESTAPI";
const api = API;

export default {
  components: {},
  props: {},
  data: () => ({
    period: 0,
    areaId: [],
    tripId: 0,
    tripUrl: "",
    areaGroup1: [],
    areaGroup2: [],
    areaGroup3: [],
    areaGroup4: [],
    areaGroup5: [],
    areaGroup6: [],
    areaGroup7: [],
    tabs: 0,
    dates: [],
    areas: [],
    dateMenu: false,
    areaMenu: false,
  }),
  created() {
    this.getAllArea();
  },
  computed: {},
  watch: {},
  methods: {
    async getAllArea() {
      this.res = await api.getAllArea();
      let res = this.res.areaList;
      this.areaGroup1 = res.slice(0, 2);
      this.areaGroup2 = res.slice(2, 33);
      this.areaGroup3 = res.slice(33, 51);
      this.areaGroup4 = res.slice(51, 79);
      this.areaGroup5 = res.slice(79, 123);
      this.areaGroup6 = res.slice(123, 160);
      this.areaGroup7 = res.slice(160);
    },
    async createTrip() {
      if (this.isLogin()) {
        return;
      }

      if (this.areaId.length == 0 || this.dates.length == 0) {
        window.alert("여행지와 여행 일정을 선택해주세요!");
      } else {
        if (this.dates.length < 2) {
          this.period = 0;
        } else {
          let a = new Date(this.dates[0]);
          let b = new Date(this.dates[1]);
          this.period = (b - a) / 86400000;
        }

        this.res = await api.createTrip(
          this.dates[0],
          this.period,
          this.areaId
        );
        this.tripId = this.res.result.tripId;
        this.tripUrl = this.res.result.tripUrl;
        this.$router.push("/plans/" + this.tripUrl);
      }
    },
    isLogin() {
      if (!this.$cookies.get("refresh")) {
        this.$emit("alert");
        return true;
      }
    },
    addArea(area) {
      if (!this.areaId.includes(area.areaId)) {
        if (this.areaId.length < 10) {
          this.areaId.push(area.areaId);
          this.areas.push(area.siName);
        } else {
          window.alert("여행지는 10개까지 선택 가능합니다.");
        }
      }
    },
    addArea2(areaId, siName) {
      if (!this.areaId.includes(areaId)) {
        if (this.areaId.length < 10) {
          this.areaId.push(areaId);
          this.areas.push(siName);
        } else {
          window.alert("여행지는 10개까지 선택 가능합니다.");
        }
      }
    },
    clearArea() {
      if (!this.areaMenu) {
        this.areaId = [];
        this.areas = [];
      }
    },
    disablePastDates(val) {
      let today = new Date();
      if (this.dates.length > 0) {
        let now = new Date(this.dates[0]);
        let today = new Date(this.dates[0]);
        let monthLater = new Date(now.setMonth(now.getMonth() + 1));
        return (
          val >=
            new Date(today.getTime() - today.getTimezoneOffset() * 60000)
              .toISOString()
              .substr(0, 10) &&
          val <
            new Date(
              monthLater.getTime() - monthLater.getTimezoneOffset() * 60000
            )
              .toISOString()
              .substr(0, 10)
        );
      } else {
        return (
          val >=
          new Date(today.getTime() - today.getTimezoneOffset() * 60000)
            .toISOString()
            .substr(0, 10)
        );
      }
    },
    getDay(day) {
      let arr = day.split("-");
      return Number(arr[arr.length - 1]);
    },
  },
};
</script>

<style>
.createTripDiv {
  display: flex;
  justify-content: center;
}
.selectAreaMenu {
  width: 30%;
}
.selectDateMenu {
  width: 30%;
}
.createTripBtn {
  height: 48px !important;
  background-color: #ff1744 !important;
  color: white !important;
  font-size: 1.4rem !important;
  font-weight: 700 !important;
}
</style>
