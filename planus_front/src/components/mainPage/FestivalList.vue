<template>
  <div style="mainInfoDiv">
    <div class="mainAreaHeader">인기지역</div>
    <v-row align="center" justify="center">
      <v-col
        v-for="(area, areaindex) in areaList"
        :key="areaindex"
        :area="area"
        cols="2"
      >
        <v-card @click="getAddArea(area.areaId, area.siName)">
          <v-img :src="area.imageUrl" height="120px"> </v-img>
          <div class="textCutting bestAreaName">{{ area.siName }}</div>
        </v-card>
      </v-col>
    </v-row>
    <div class="mainInfoDivider"></div>
    <div class="mainFestivalHeader">축제정보</div>
    <v-row align="center" justify="center" class="pl-10 pr-10">
      <v-col
        v-for="(festival, festivalindex) in festivalList"
        :key="festivalindex"
        :festival="festival"
        cols="3"
      >
        <v-card @click="getAddArea(festival.areaId, festival.siName)">
          <v-img :src="festival.imageUrl" height="300px"> </v-img>
          <v-card-title class="textCutting festivalName">{{
            festival.title
          }}</v-card-title>
          <v-card-subtitle class="textCutting">{{
            festival.address
          }}</v-card-subtitle>
          <v-card-text class="textCutting">
            시작: {{ festival.startDate }}<br />종료: {{ festival.endDate }}
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    <v-row justify="center" class="pagenation-bar">
      <v-pagination
        v-model="currentPage"
        class="my-4"
        :length="totalPage"
        :total-visible="7"
        color="#4a8072"
      ></v-pagination>
    </v-row>
  </div>
</template>

<script>
import API from "@/api/RESTAPI";
const api = API;

export default {
  data: () => ({
    areaList: [],
    currentPage: 1,
    festivalList: [],
    totalPage: 0,
  }),
  async created() {
    await this.getBestArea();
    await this.getFestival(1);
  },
  watch: {
    currentPage(newVal) {
      this.getFestival(newVal);
    },
  },
  methods: {
    async getBestArea() {
      this.res = await api.getBestArea();
      this.areaList = this.res.areaList;
    },
    async getFestival(page) {
      this.res = await api.getFestival(page - 1);
      this.festivalList = this.res.festivalPage.festivalList;
      this.totalPage = this.res.festivalPage.totalPage;
    },
    getAddArea(areaId, siName) {
      this.$emit("getAddArea", areaId, siName);
    },
  },
};
</script>

<style>
.mainAreaHeader {
  display: flex;
  padding: 3%;
  justify-content: center;
  font-size: 2.2rem;
  font-weight: 700;
  color: white;
  text-shadow: 1px 2px gray;
}
.mainFestivalHeader {
  display: flex;
  padding-bottom: 3%;
  justify-content: center;
  font-size: 2.2rem;
  font-weight: 700;
  color: white;
  text-shadow: 1px 2px gray;
}
.bestAreaName {
  padding: 4%;
  text-align: center;
  font-size: 1.2rem;
  font-weight: 700;
  color: #383d3c;
}
.festivalName {
  display: block;
  font-size: 1.2rem;
  font-weight: 700;
  color: #383d3c;
}
.mainInfoDivider {
  width: 90%;
  margin: 6% auto 3%;
  border: 2px solid;
  border-color: white;
  background-color: white;
}
</style>
