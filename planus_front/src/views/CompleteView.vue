<template>
  <div>
    <div class="mainHeaderDiv">
      <div>
        <button class="mainPageBtn" @click="goToCommunity">
          <span>커뮤니티</span>
        </button>
        <button class="mainPageBtn" @click="goToMypage">
          <span>마이페이지</span>
        </button>
      </div>
      <login-button></login-button>
    </div>
    <v-container>
      <div id="parentDiv" style="overflow: hidden">
        <v-sheet
          id="capture"
          height="75vh"
          class="content pa-4"
          justify="center"
        >
          <complete-map
            :tripArea="tripInfo.tripArea"
            :completeList="completeList"
            @readyToCapture="captureAndUpload"
          ></complete-map>
          <complete-page :completeList="completeList"></complete-page>
        </v-sheet>
      </div>
      <v-container class="d-flex justify-center">
        <v-btn outlined large class="mx-4" color="#4a8072" @click="captureImg"
          >저장</v-btn
        >
        <v-btn outlined large class="mx-4" color="#4a8072" @click="shareTrip"
          >공유</v-btn
        >
        <v-btn outlined large class="mx-4" color="#4a8072" @click="modalOn"
          >복사</v-btn
        >
      </v-container>

      <v-dialog v-model="alert" max-width="450">
        <v-card>
          <v-card-title></v-card-title>
          <v-card-text color="white"> 로그인 후 이용해주세요</v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="#4a8072" outlined @click="alert = false">확인</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
      <v-dialog v-model="modal" max-width="450">
        <v-card>
          <v-card-title class="d-flex justify-center modal-title"
            >여행 시작일을 선택해주세요</v-card-title
          >
          <v-row justify="center" class="ma-0 pa-0">
            <v-date-picker
              v-model="newStartDate"
              :allowed-dates="disablePastDates"
              no-title
              scrollable
              locale="ko-KR"
              :day-format="getDay"
              color="#4a8072"
            ></v-date-picker>
          </v-row>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="#4a8072" outlined @click="copyTrip">일정생성</v-btn>
            <v-btn color="#4a8072" outlined @click="modal = false">취소</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-container>
  </div>
</template>

<script>
import html2canvas from "html2canvas";
import API from "@/api/RESTAPI";
import CompletePage from "@/components/complete/CompletePage.vue";
import CompleteMap from "@/components/complete/CompleteMap.vue";
import LoginButton from "@/components/KakaoLogin/LoginButton.vue";
const api = API;

import axios from "axios";

export default {
  name: "CompleteView",
  components: {
    CompletePage,
    CompleteMap,
    LoginButton,
  },
  data() {
    return {
      tripUrl: "",
      tripInfo: {
        tripId: 0,
        admin: 0,
        startDate: "",
        period: 0,
        memberOrAdmin: 0,
        complete: false,
        imageUrl: "",
        tripArea: [
          {
            areaId: 0,
            doName: "",
            siName: "",
            lat: 0,
            lng: 0,
          },
        ],
      },
      memberList: [
        {
          userId: 0,
          name: "",
          email: "",
        },
      ],
      completeList: [],
      alert: false,
      modal: false,
      newStartDate: null,
      // mapCnt: 0,
    };
  },
  async created() {
    this.tripUrl = this.$route.params.tripUrl;
    await this.getTripInfo();
    await this.getMemberList();
    await this.getCompleteInfo();
  },
  methods: {
    async getTripInfo() {
      this.res = await api.getTripInfo(this.tripUrl).catch(() => {
        window.alert("존재하지 않는 url입니다!");
        this.$router.push("/");
      });
      this.tripInfo = this.res.result;
      if (!this.tripInfo.complete) {
        this.$router.push("/plans/" + this.tripUrl);
      }
    },
    async getMemberList() {
      this.res = await api.getMemberList(this.tripInfo.tripId);
      this.memberList = this.res.memberList;
    },
    async getCompleteInfo() {
      this.res = await api.getComplete(this.tripUrl);
      this.completeList = this.res.result.planResDTOList;
    },
    async captureAndUpload() {
      // image 이미 있으면 return
      if (this.tripInfo.imageUrl) return;
      // 방장 아니면 return
      if (this.tripInfo.memberOrAdmin != 2) return;

      // 요청 2번 오류 지속될경우 적용...!
      // if (++this.mapCnt != 2) return;

      // 이미지 캡쳐
      const el = document.getElementById("capture");
      el.style.height = el.scrollHeight + "px";
      html2canvas(el, {
        backgroundColor: null,
        useCORS: true,
      }).then((canvas) => {
        // 이미지 데이터
        let dataUrl = canvas.toDataURL("image/png");

        // 이미지 데이터 > 이미지 파일 변환
        let byteString = window.atob(dataUrl.split(",")[1]);
        let array = [];
        for (var i = 0; i < byteString.length; i++) {
          array.push(byteString.charCodeAt(i));
        }
        let myBlob = new Blob([new Uint8Array(array)], { type: "image/png" }); // blob 생성
        let file = new File([myBlob], this.tripUrl + ".png"); // blob > file 변환

        // multipart-formdata
        let formData = new FormData();
        formData.append("file", file);
        formData.append(
          "tripId",
          new Blob([JSON.stringify(this.tripInfo.tripId)], {
            type: "application/json",
          })
        );

        // upload api
        axios({
          url: process.env.VUE_APP_API_URL + "/complete/image",
          method: "POST",
          data: formData,
        })
          .then((res) => {
            if (res.data != "fail") this.tripInfo.imageUrl = res.data;
          })
          .catch((err) => {
            console.log(err);
          });
      });
      el.style.height = "75vh";
    },
    captureImg() {
      const el = document.getElementById("capture");
      el.style.height = el.scrollHeight + "px";
      html2canvas(el, {
        backgroundColor: null,
        useCORS: true,
      }).then((canvas) => {
        this.saveAs(canvas.toDataURL("image/png"), "여행일정.png");
      });
      el.style.height = "75vh";
    },
    saveAs(uri, filename) {
      let link = document.createElement("a");
      if (typeof link.download === "string") {
        link.href = uri;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      } else {
        window.open(uri);
      }
    },
    shareTrip() {
      let imgUrl = this.tripInfo.imageUrl;
      let pgUrl = window.location.href;

      if (this.tripInfo.imageUrl == null) {
        imgUrl = this.tripInfo.imageUrl;
      }

      this.$kakao.Link.sendDefault({
        objectType: "feed",
        content: {
          title: "Planus",
          description: "Planus에서 완성된 여행 일정을 확인해 보세요!",
          imageUrl: imgUrl,
          link: {
            mobileWebUrl: imgUrl,
            webUrl: imgUrl,
          },
        },
        buttons: [
          {
            title: "일정 확인하기",
            link: {
              mobileWebUrl: pgUrl,
              webUrl: pgUrl,
            },
          },
        ],
      });
    },
    modalOn() {
      if (!this.$cookies.get("refresh")) {
        this.alert = true;
        return;
      }
      this.modal = true;
    },
    async copyTrip() {
      if (!this.newStartDate) {
        window.alert("여행 시작일을 선택해주세요");
        return;
      }
      let newTripUrl = await api.copyTrip(
        this.tripInfo.tripId,
        this.newStartDate
      );
      this.modal = false;
      if (newTripUrl) {
        this.$router.push("/plans/" + newTripUrl);
      }
    },
    goHome() {
      this.$router.push("/");
    },
    disablePastDates(val) {
      let today = new Date();
      return (
        val >=
        new Date(today.getTime() - today.getTimezoneOffset() * 60000)
          .toISOString()
          .substr(0, 10)
      );
    },
    getDay(day) {
      let arr = day.split("-");
      return Number(arr[arr.length - 1]);
    },
    goToCommunity() {
      this.$router.push("/community");
    },
    goToMypage() {
      if (this.$cookies.get("refresh") == null) {
        this.alert = !this.alert;
      } else {
        this.$router.push("/mypage");
      }
    },
  },
};
</script>

<style scoped>
.content {
  border: solid;
  border-width: 5px;
  border-color: #4a8072;
  border-radius: 10px;
  overflow: auto;
}
.content::-webkit-scrollbar {
  color: #544c4c;
  width: 10px;
}
.content::-webkit-scrollbar-thumb {
  background-color: #544c4c;
  border-radius: 10px;
}
.modal-title {
  color: #4a8072;
}
</style>
