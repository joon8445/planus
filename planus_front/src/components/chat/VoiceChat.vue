<template>
  <div style="position: relative; display: flex">
    <v-icon @click="goHome">mdi-home</v-icon>
    <v-btn
      color="#4A8072"
      outlined
      v-if="!isConnected"
      @click="joinBtnClick"
      class="pa-2"
      >음성채팅 참여</v-btn
    >
    <v-btn
      color="#4A8072"
      outlined
      v-if="isConnected"
      @click="exitBtnClick"
      class="pa-1"
      >음성채팅 나가기</v-btn
    >
    <v-btn
      depressed
      color="#4A8072"
      outlined
      fab
      small
      v-if="isConnected"
      @click="micBtnHandler"
      style="box-shadow: 1px 1px 1px 1px #544c4c50; margin-top: 3px"
      class="ml-3 mr-1"
    >
      <v-icon v-if="micOn">mdi-microphone</v-icon>
      <v-icon v-if="!micOn">mdi-microphone-off</v-icon>
    </v-btn>

    <v-container d-flex v-if="isConnected" style="padding: 0">
      <v-col style="flex-grow: 0; padding: 0">
        <user-video :stream-manager="publisher" :idx="0" />
      </v-col>
      <v-col
        style="flex-grow: 0; padding: 0"
        v-for="(sub, idx) in subscribers"
        :key="sub.stream.connection.connectionId"
      >
        <user-video :stream-manager="sub" :idx="idx + 1" />
      </v-col>
    </v-container>
  </div>
</template>

<script>
import { OpenVidu } from "openvidu-browser";
import axios from "axios";
import UserVideo from "@/components/chat/UserVideo.vue";

const OPENVIDU_SERVER_URL = process.env.VUE_APP_OPENVIDU_SERVER_URL;
const OPENVIDU_SERVER_SECRET = process.env.VUE_APP_OPENVIDU_SERVER_SECRET;
const api = axios;
api.defaults.headers.post["Content-Type"] = "application/json";

export default {
  data() {
    return {
      OV: undefined,
      session: undefined,
      publisher: undefined,
      subscribers: [],
      micOn: false,
      isConnected: false,
    };
  },
  props: {
    tripId: Number,
    nickname: String,
  },
  components: {
    UserVideo,
  },

  methods: {
    goHome() {
      this.leaveSession();
      this.$router.push("/");
      this.$router.go();
    },
    joinBtnClick() {
      this.joinSession();
    },
    exitBtnClick() {
      this.leaveSession();
    },
    micBtnHandler() {
      if (this.isConnected) {
        this.micOn = !this.micOn;
        this.publisher.publishAudio(this.micOn);
      }
    },

    joinSession() {
      // --- Get an OpenVidu object ---
      this.OV = new OpenVidu();

      this.OV.enableProdMode();

      // --- Init a session ---
      this.session = this.OV.initSession();
      this.session.on("streamCreated", ({ stream }) => {
        const subscriber = this.session.subscribe(stream);
        this.subscribers.push(subscriber);
      });

      this.session.on("streamDestroyed", ({ stream }) => {
        const index = this.subscribers.indexOf(stream.streamManager, 0);
        if (index >= 0) {
          this.subscribers.splice(index, 1);
        }
      });
      this.session.on("exception", ({ exception }) => {
        console.warn(exception);
      });
      this.getToken(this.tripId).then(async (token) => {
        this.session
          .connect(token, { clientData: this.nickname })
          .then(async () => {
            let publisher = this.OV.initPublisher(undefined, {
              audioSource: undefined, // The source of audio. If undefined default microphone
              videoSource: false, // The source of video. If undefined default webcam
              publishAudio: false, // Whether you want to start publishing with your audio unmuted or not
              publishVideo: false, // Whether you want to start publishing with your video enabled or not
            });
            this.publisher = publisher;

            this.session.publish(this.publisher);
          })
          .then(() => {
            this.isConnected = true;
          })
          .catch((error) => {
            console.error(
              "There was an error connecting to the session:",
              error.code,
              error.message
            );
          });
      });

      window.addEventListener("beforeunload", this.leaveSession);
      window.addEventListener("popstate", this.leaveSession);
    },
    leaveSession() {
      // --- Leave the session by calling 'disconnect' method over the Session object ---
      if (this.session) this.session.disconnect();

      this.session = undefined;
      this.mainStreamManager = undefined;
      this.publisher = undefined;
      this.subscribers = [];
      this.OV = undefined;
      this.isConnected = false;
      window.removeEventListener("beforeunload", this.leaveSession);
    },

    async getToken(mySessionId) {
      return this.createSession(mySessionId).then((sessionId) =>
        this.createToken(sessionId)
      );
    },
    createSession(sessionId) {
      return new Promise((resolve, reject) => {
        api
          .post(
            `${OPENVIDU_SERVER_URL}/openvidu/api/sessions`,
            JSON.stringify({
              customSessionId: sessionId + "",
            }),
            {
              auth: {
                username: "OPENVIDUAPP",
                password: OPENVIDU_SERVER_SECRET,
              },
            }
          )
          .then((response) => response.data)
          .then((data) => resolve(data.id))
          .catch((error) => {
            if (error.response.status === 409) {
              resolve(sessionId);
            } else {
              console.warn(
                `No connection to OpenVidu Server. This may be a certificate error at ${OPENVIDU_SERVER_URL}`
              );
              if (
                window.confirm(
                  `${sessionId} No connection to OpenVidu Server. This may be a certificate error at ${OPENVIDU_SERVER_URL}\n\nClick OK to navigate and accept it. If no certificate warning is shown, then check that your OpenVidu Server is up and running at "${OPENVIDU_SERVER_URL}"`
                )
              ) {
                location.assign(`${OPENVIDU_SERVER_URL}/accept-certificate`);
              }
              reject(error.response);
            }
          });
      });
    },

    createToken(sessionId) {
      return new Promise((resolve, reject) => {
        api
          .post(
            `${OPENVIDU_SERVER_URL}/openvidu/api/sessions/${sessionId}/connection`,
            {},
            {
              auth: {
                username: "OPENVIDUAPP",
                password: OPENVIDU_SERVER_SECRET,
              },
            }
          )
          .then((response) => response.data)
          .then((data) => resolve(data.token))
          .catch((error) => reject(error.response));
      });
    },
  },
};
</script>

<style></style>
