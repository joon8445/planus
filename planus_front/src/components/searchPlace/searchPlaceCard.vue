<template>
  <v-container class="pa-1">
    <v-card outlined flat class="mx-0 my-1" @click="addMarker">
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="text-h6 mb-1">
            {{ searchedPlace.place }}
          </v-list-item-title>
          <v-list-item-subtitle>{{
            searchedPlace.address
          }}</v-list-item-subtitle>
        </v-list-item-content>

        <v-list-item-avatar tile size="80"
          ><v-img v-if="searchedPlace.photo" :src="searchedPlace.photo" />
        </v-list-item-avatar>
      </v-list-item>

      <v-card-actions>
        <v-row>
          <plan-modal
            v-show="memberOrAdmin == 2"
            @planSubmit="planSubmit"
            :fromBucket="false"
            class="col-6"
          ></plan-modal>
          <v-spacer></v-spacer>
          <v-col class="col-6">
            <v-btn
              small
              width="100%"
              color="#4A8072"
              class="white--text"
              @click="bucketClick"
              >버킷리스트 추가</v-btn
            >
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script>
import PlanModal from "@/components/recommend/PlanModal.vue";
import Black from "@/assets/default_image.png";
import { mapState, mapMutations } from "vuex";
const mapStore = "mapStore";

export default {
  components: { PlanModal },
  data: () => {
    return {
      blackImage: Black,
    };
  },
  comments: {
    PlanModal,
  },
  props: {
    searchedPlace: Object,
    memberOrAdmin: Number,
  },
  computed: {
    ...mapState(mapStore, ["spotInfo"]),
  },
  methods: {
    ...mapMutations(mapStore, ["SET_SPOT_INFO"]),
    bucketClick() {
      this.$emit(
        "addBucket",
        this.searchedPlace.place,
        this.searchedPlace.address,
        this.searchedPlace.lat,
        this.searchedPlace.lng
      );

      this.SET_SPOT_INFO(null);
    },
    planSubmit(costTime, fromBucket) {
      this.$emit(
        "addTimetable",
        costTime,
        this.searchedPlace.place,
        this.searchedPlace.lat,
        this.searchedPlace.lng,
        fromBucket
      );

      this.SET_SPOT_INFO(null);
    },
    addMarker() {
      this.SET_SPOT_INFO(null);
      this.SET_SPOT_INFO({
        place: this.searchedPlace.place,
        address: this.searchedPlace.address,
        lat: this.searchedPlace.lat,
        lng: this.searchedPlace.lng,
      });
    },
  },
};
</script>

<style scoped>
.theme--light.v-sheet--outlined {
  border: solid 1px #4a8072;
}
</style>
