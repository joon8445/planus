<template>
  <v-container class="px-1">
    <bucket-card
      v-for="(bucket, i) in bucketList"
      :key="i + 'b'"
      :bucket="bucket"
      :memberOrAdmin="memberOrAdmin"
      @delBucket="delBucket"
      @addTimetable="addTimetable"
    ></bucket-card>
  </v-container>
</template>

<script>
import API from "@/api/RESTAPI";
const api = API;

import BucketCard from "@/components/bucketList/BucketCard.vue";
export default {
  name: "BucketList",
  components: {
    BucketCard,
  },
  data: function () {
    return {
      bucketList: [],
    };
  },
  props: {
    tripId: Number,
    deletedBucket: Object,
    addedBucket: Object,
    memberOrAdmin: Number,
    addedTimetable: Object,
  },

  watch: {
    deletedBucket() {
      // 삭제 로직
      let item = {
        place: this.deletedBucket.place,
        address: this.deletedBucket.address,
        lat: this.deletedBucket.lat,
        lng: this.deletedBucket.lng,
      };
      for (const idx of this.bucketList.keys()) {
        if (JSON.stringify(this.bucketList[idx]) == JSON.stringify(item)) {
          this.bucketList.splice(idx, 1);
        }
      }
    },
    addedBucket() {
      this.bucketList.push(this.addedBucket);
    },
    addedTimetable() {
      let item = {
        place: this.addedTimetable.place,
        address: this.addedTimetable.address,
        lat: this.addedTimetable.lat,
        lng: this.addedTimetable.lng,
      };
      for (const idx of this.bucketList.keys()) {
        if (JSON.stringify(this.bucketList[idx]) == JSON.stringify(item)) {
          // bucketList에서 제거
          this.bucketList.splice(idx, 1);
          break;
        }
      }
    },
    bucketList(newVal) {
      this.$emit("changeBucketList", newVal);
    },
    tripId() {
      this.getBucketList();
    },
  },
  methods: {
    async getBucketList() {
      this.bucketList = await api.getBucketList(this.tripId);
    },
    delBucket(bucket) {
      this.$emit("delBucket", bucket);
    },
    addTimetable(costTime, place, lat, lng, fromBucket, address) {
      this.$emit(
        "addTimetable",
        costTime,
        place,
        lat,
        lng,
        fromBucket,
        address
      );
    },
  },
};
</script>
<style></style>
