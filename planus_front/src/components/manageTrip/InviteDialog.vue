<template>
  <v-dialog v-model="dialog" max-width="900">
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        depressed
        color="#4A8072"
        outlined
        v-bind="attrs"
        v-on="on"
        @click="getMemberList"
        class="mr-1"
      >
        초대
      </v-btn>
    </template>
    <v-card style="margin: auto">
      <v-card-actions>
        <v-spacer></v-spacer>
      </v-card-actions>
      <v-card-title class="dialogtitle">
        <v-row>
          <v-col>참가자 목록 </v-col>
          <v-spacer></v-spacer>
          <v-col align="end">
            <v-btn depressed color="#4A8072" outlined @click="copyUrl"
              >링크 복사</v-btn
            ></v-col
          ></v-row
        >
      </v-card-title>
      <v-card-text v-for="(member, i) in memberList" :key="i" :member="member">
        <v-row align="center">
          <v-col>
            <v-btn
              depressed
              rounded
              color="#4A8072"
              v-if="admin == member.userId"
              ><span class="font-weight-bold" style="color: white"
                >방장</span
              ></v-btn
            >
            <v-btn
              depressed
              rounded
              color="#544C4C"
              v-else-if="admin == userId"
              @click="changeAdmin(member.userId)"
              ><span class="font-weight-bold" style="color: white"
                >방장변경</span
              ></v-btn
            >
          </v-col>
          <v-col>
            <div class="memberName">{{ member.name }}</div>
          </v-col>
          <v-col>
            <div class="memberEmail" v-show="member.email != null">
              ({{ member.email }})
            </div>
          </v-col>
          <v-col>
            <v-btn
              depressed
              rounded
              color="#4A8072"
              v-if="connector.includes(member.userId + '')"
              ><span class="font-weight-bold" style="color: white"
                >접속</span
              ></v-btn
            >
            <v-btn depressed rounded outlined color="#4A8072" white--text v-else
              >접속안함</v-btn
            >
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
import API from "@/api/RESTAPI";
const api = API;

export default {
  data() {
    return {
      dialog: false,
      memberList: [
        {
          userId: 0,
          name: "",
          email: "",
        },
      ],
    };
  },
  props: {
    tripId: Number,
    userId: Number,
    tripUrl: String,
    admin: Number,
    connector: Array,
  },
  created() {},
  methods: {
    async getMemberList() {
      this.$emit("getConnector");
      this.res = await api.getMemberList(this.tripId);
      this.memberList = this.res.memberList;
    },
    copyUrl() {
      navigator.clipboard.writeText(window.location.href).then(() => {
        window.alert("링크가 복사되었습니다! 친구를 초대해보세요!");
      });
    },
    changeAdmin(newAdminId) {
      this.$emit("changeAdmin", newAdminId);
    },
  },
};
</script>

<style></style>
