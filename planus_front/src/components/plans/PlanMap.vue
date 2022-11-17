<template>
  <div style="position: relative">
    <div class="mapAreaBtnDiv">
      <v-btn
        rounded
        class="focusAreaBtn"
        v-for="(area, areaIdx) in tripArea"
        :key="areaIdx"
        @click="clickLocation(area, 12)"
        >{{ area.siName }}</v-btn
      >
    </div>
    <gmap-map
      :zoom="zoom"
      :center="center"
      style="width: 100%; height: 85vh"
      @zoom_changed="getCurrentZoom"
      @center_changed="getCurrentCenter"
    >
      <gmap-marker
        :icon="{
          url: require('@/assets/BucketMarker.png'),
          scaledSize: { width: 40, height: 40 },
          labelOrigin: { x: 20, y: -10 },
        }"
        :key="index + 'b'"
        v-for="(m, index) in bucketList"
        :position="m"
        :label="{
          text: m.place.substring(0, 2),
          color: '#4A8072',
          fontWeight: 'bold',
        }"
        @click="clickLocation(m, 15), clickBucketMarker(index)"
        id="bucket"
        ><gmap-info-window
          v-if="bucketInfo[index]"
          :opened="bucketInfo[index]"
          @closeclick="bucketInfo[index] = false"
        >
          <h3>{{ bucketList[index].place }}</h3>
          <h6>{{ bucketList[index].address }}</h6>
        </gmap-info-window></gmap-marker
      >
      <gmap-marker
        :key="index + 'p'"
        v-for="(m, index) in timetableList"
        :position="m"
        :label="{
          text: m.place.substring(0, 2),
          color: '#FF1744',
          fontWeight: 'bold',
        }"
        @click="clickLocation(m, 15)"
        :icon="{
          url: require('@/assets/PlanMarker.png'),
          scaledSize: { width: 40, height: 40 },
          labelOrigin: { x: 20, y: -10 },
        }"
        :zIndex="999999"
        id="plan"
      ></gmap-marker>
      <gmap-polyline
        v-if="planList"
        :path.sync="planList"
        :options="{ strokeColor: '#FF1744', strokeWeight: 2 }"
      >
      </gmap-polyline>
      <gmap-marker
        :icon="{
          url: require('@/assets/InfoMarker.png'),
          scaledSize: { width: 40, height: 40 },
        }"
        v-if="spotInfo"
        :position="spotInfo"
        @click="clickLocation(spotInfo, 15), (isInfo = !isInfo)"
        id="bucket"
      >
        <gmap-info-window
          v-if="spotInfo"
          :opened="isInfo"
          @closeclick="isInfo = false"
        >
          <h3>{{ spotInfo.place }}</h3>
          <h6>{{ spotInfo.address }}</h6>
        </gmap-info-window>
      </gmap-marker>
    </gmap-map>
  </div>
</template>

<script>
import { mapState, mapMutations } from "vuex";
const mapStore = "mapStore";

export default {
  name: "PlanMap",

  data() {
    return {
      center: {},
      zoom: 12,
      nowCenter: {},
      planList: [],
      isInfo: false,
      bucketInfo: [],
    };
  },
  props: {
    tripArea: Array,
    bucketList: Array,
    timetableList: Array,
  },
  computed: {
    ...mapState(mapStore, ["spotInfo"]),
  },
  methods: {
    ...mapMutations(mapStore, ["SET_SPOT_INFO"]),
    clickLocation(loc, zoom) {
      setTimeout(() => {
        this.center = this.nowCenter;
      }, 50);
      setTimeout(() => {
        this.center = loc;
        this.nowCenter = loc;
        this.zoom = zoom;
      }, 50);
    },
    getCurrentCenter(center) {
      this.nowCenter = { lat: center.lat(), lng: center.lng() };
    },
    getCurrentZoom(zoom) {
      this.zoom = zoom;
    },
    clickBucketMarker(index) {
      for (let i = 0; i < this.bucketInfo.length; i++) {
        if (this.bucketInfo[i]) this.bucketInfo[i] = false;
      }
      this.bucketInfo[index] = true;
    },
  },
  watch: {
    spotInfo(newVal) {
      if (!newVal) {
        this.isInfo = false;
        return;
      }
      this.bucketList.forEach((bucket) => {
        if (bucket.lat == newVal.lat && bucket.lng == newVal.lng) {
          this.SET_SPOT_INFO(null);
          return;
        }
      });
      this.timetableList.forEach((timetable) => {
        if (timetable.lat == newVal.lat && timetable.lng == newVal.lng) {
          this.SET_SPOT_INFO(null);
          return;
        }
      });
      this.isInfo = true;
      this.clickLocation(newVal, 15);
    },
    tripArea(newVal) {
      this.center = newVal[0];
      this.nowCenter = newVal[0];
    },
    timetableList(newVal) {
      this.planList = newVal;
    },
    nowCenter(newVal) {
      this.$emit("getCenter", newVal);
    },
  },
  destroyed() {
    this.SET_SPOT_INFO(null);
  },
};
</script>

<style>
a[href^="http://maps.google.com/maps"]
{
  display: none !important;
}
a[href^="https://maps.google.com/maps"]
{
  display: none !important;
}
.gmnoprint a,
.gmnoprint span,
.gm-style-cc {
  display: none;
}
.gmnoprint div {
  background: none !important;
}
.gmnoprint div {
  display: none;
}
.gm-fullscreen-control {
  display: none;
}
.mapAreaBtnDiv {
  width: 80px;
  position: absolute;
  left: 10px;
  top: 10px;
  z-index: 1;
}
.focusAreaBtn {
  margin-bottom: 10px;
  background-color: #4a8072 !important;
  color: white !important;
}
</style>
