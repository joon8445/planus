<template>
  <v-container class="shared-container">
    <v-row>
      <v-col v-for="(trip, index) in tripList" :key="index" cols="6">
        <v-card
          outlined
          class="mx-10 mt-2"
          @click="goToTrip(trip.tripUrl)"
          height="19vh"
          style="position: relative"
        >
          <v-icon
            @click.stop="deleteTrip(trip.tripId)"
            style="position: absolute; right: 0; z-index: 1"
            >mdi-close</v-icon
          >
          <v-list-item>
            <v-list-item-avatar tile class="my-0 img-avatar"
              ><v-img :src="trip.imageUrl"></v-img>
            </v-list-item-avatar>
            <v-list-item-content>
              <div class="text-overline mb-4">
                <span class="txt_line"
                  >{{ trip.startDate }} ~ {{ trip.endDate }}</span
                >
              </div>
              <v-list-item-title class="text-h5 mb-1">
                <span>| </span>
                <span v-for="(area, idx) in trip.areaList" :key="idx + 'a'"
                  >{{ area }} |
                </span>
              </v-list-item-title>
              <v-list-item-subtitle
                class="d-flex align-end flex-column my-0 py-0"
              >
                <span
                  ><v-icon v-if="trip.complete" color="#4a8072"
                    >mdi-file-check</v-icon
                  >
                  <v-icon v-else color="#4a8072">mdi-file-document-edit</v-icon>
                  &nbsp;{{ trip.participants }}명
                </span>
              </v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-col>
    </v-row>
    <v-spacer></v-spacer>
    <v-row justify="center" class="pagenation-bar">
      <v-pagination
        v-model="currentPage"
        class="my-4"
        :length="totalPage"
        color="#4a8072"
      ></v-pagination>
    </v-row>
  </v-container>
</template>

<script>
import API from "@/api/RESTAPI";
const api = API;

export default {
  name: "SharedTripList",

  data() {
    return {
      currentPage: 1,
      tripList: null,
      totalPage: null,
    };
  },
  async created() {
    await this.getSharedTripList(1);
  },
  watch: {
    currentPage(newVal) {
      this.getSharedTripList(newVal);
    },
  },
  methods: {
    async getSharedTripList(page) {
      let res = await api.getMyTripShared(page - 1);
      this.totalPage = res.totalPage;
      this.tripList = res.tripList;
    },
    goToTrip(tripUrl) {
      this.$router.push("/plans/" + tripUrl);
    },
    async deleteTrip(tripId) {
      await api.deleteTrip(tripId);
      await this.getSharedTripList(this.currentPage);
      if (this.currentPage > this.totalPage) this.currentPage--;
    },
  },
};
</script>

<style scoped>
.v-list-item {
  padding-left: 0;
}
.v-card {
  border-radius: 0;
  border-color: #4a8072 !important;
  overflow: hidden;
}
.shared-container {
  height: 100%;
  position: relative;
}
.pagenation-bar {
  width: 100%;
  position: absolute;
  bottom: 0;
}
.img-avatar {
  height: 100% !important;
  width: 40% !important;
}
.v-card > *:last-child:not(.v-btn):not(.v-chip):not(.v-avatar) {
  height: -webkit-fill-available;
}
.txt_line {
  width: 80%;
  padding: 0 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
