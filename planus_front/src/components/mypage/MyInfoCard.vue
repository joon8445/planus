<template>
  <v-container>
    <v-row class="d-flex justify-center mt-12">
      <img :src="imgUrl" width="160px" height="160px" />
    </v-row>
    <v-row class="d-flex justify-center mt-10">
      <h3>
        <span class="font-weight-bold" style="color: #4a8072">닉네임</span>
      </h3>
    </v-row>
    <v-row class="d-flex justify-center">
      <v-col cols="4">
        <v-text-field
          v-model="userNickname"
          rounded
          outlined
          :rules="[rules.special, rules.required, rules.space, rules.maxCount]"
          :readonly="!modify"
          dense
          counter
          maxlength="20"
          color="#4a8072"
          class="centered-input font-weight-bold"
          @keyup.enter="saveChange"
        >
        </v-text-field>
      </v-col>
    </v-row>
    <v-row class="d-flex justify-center">
      <v-btn v-if="modify" @click="saveChange" color="#4A8072"
        ><span class="font-weight-bold" style="color: white">저장</span></v-btn
      >
      <v-btn v-else @click="toModify" color="#4A8072"
        ><span class="font-weight-bold" style="color: white">수정</span></v-btn
      >
    </v-row>
  </v-container>
</template>

<script>
import API from "@/api/RESTAPI";
import jwt_decode from "jwt-decode";
const api = API;
export default {
  name: "MyInfoCard",
  created() {
    this.getMyInfo();
  },
  data: () => {
    return {
      originNickname: "",
      userNickname: "",
      modify: false,
      imgUrl: "",
      validateMessage: "12345",
      rules: {
        space: (value) => {
          return (
            (value.indexOf(" ") == -1 && value.indexOf("　") == -1) ||
            "공백은 사용할 수 없습니다."
          );
        },
        required: (value) => {
          return !!value || "닉네임을 입력해주세요.";
        },
        special: (value) => {
          return (
            !/[~!@#$%^&*()_+|<>?/:{}]/.test(value) ||
            "특수문자는 사용할 수 없습니다."
          );
        },
        maxCount: (value) => {
          return value.length <= 20 || "닉네임은 20자를 초과할 수 없습니다.";
        },
      },
    };
  },
  methods: {
    async getMyInfo() {
      await api.dummy();
      var jwt = jwt_decode(this.$cookies.get("token"));
      this.userNickname = jwt.nickname;
      this.originNickname = jwt.nickname;
      this.imgUrl = jwt.imageUrl;
    },
    toModify() {
      this.modify = !this.modify;
    },
    async saveChange() {
      var nickname = this.userNickname;
      if (nickname == this.originNickname) {
        this.toModify();
      } else {
        if (this.validate(this.userNickname)) {
          var result = await api.changeMyInfo(this.userNickname);
          var token = result.newToken;
          this.$cookies.set("token", token, 30 * 60);
          this.originNickname = this.userNickname;
          this.toModify();
        }
      }
    },
    space(value) {
      return value.indexOf(" ") == -1 && value.indexOf("　") == -1;
    },
    required(value) {
      return !!value;
    },
    special(value) {
      return !/[~!@#$%^&*()_+|<>?:{}]/.test(value);
    },
    maxCount(value) {
      return value.length <= 20;
    },
    validate(value) {
      return (
        this.space(value) &&
        this.required(value) &&
        this.special(value) &&
        this.maxCount(value)
      );
    },
  },
};
</script>

<style scoped>
.centered-input >>> input {
  text-align: center;
}
</style>
