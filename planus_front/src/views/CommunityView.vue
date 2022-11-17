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
    <br />
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
    <search-article @goSearch="goSearch"></search-article>
    <v-container class="made-container">
      <v-row>
        <v-col v-for="(article, i) in articleList" :key="i" cols="6">
          <v-card
            outlined
            class="mx-10 mt-2"
            @click="goToArticle(article.articleId)"
            style="border-color: #4a8072"
          >
            <v-list-item class="pa-0">
              <v-list-item-avatar tile class="my-0 img-avatar" size="50%">
                <v-img :src="article.imageUrl"></v-img>
              </v-list-item-avatar>
              <v-list-item-content class="pa-2">
                <div>
                  <v-list-item-title>
                    <v-card-title class="pa-0 textCutting">
                      {{ article.title }}
                    </v-card-title>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    <v-icon small color="#ff1744">mdi-heart</v-icon>
                    <span> {{ article.likes }}</span>
                    <v-icon small color="#4a8072">mdi-eye</v-icon>
                    <span> {{ article.hits }}</span>
                    &nbsp;
                  </v-list-item-subtitle>
                </div>
                <v-chip-group>
                  <v-chip
                    color="#4a8072"
                    text-color="white"
                    v-for="(area, idx) in article.areaList"
                    :key="idx + 'a'"
                    >{{ area }}
                  </v-chip>
                </v-chip-group>
                <div class="textCutting" style="text-align: right">
                  {{ article.name }}
                </div>
                <div class="textCutting" style="text-align: right">
                  ({{ article.regDate.split("T")[0] }}
                  {{ article.regDate.split("T")[1].split(".")[0] }})
                </div>
              </v-list-item-content>
            </v-list-item>
            <!-- <v-row class="pa-0">
            <v-col cols="4">
              <v-img :src="article.imageUrl" height="100%"> </v-img>
            </v-col>
            <v-col cols="8">
              <v-row class="pa-0">
                <v-col cols="6">
                  <v-card-title>{{ article.title }}</v-card-title>
                </v-col>
                <v-col
                  cols="6"
                  class="d-flex"
                  style="align-self: center; justify-content: end"
                >
                  <v-icon>mdi-eye</v-icon>
                  {{ article.hits }}
                  &nbsp;
                  <v-icon>mdi-heart</v-icon>
                  {{ article.likes }}
                </v-col>
              </v-row>
              <v-row class="pa-0">
                <v-col>
                  <v-chip-group column>
                    <v-chip
                      v-for="(area, idx) in article.areaList"
                      :key="idx + 'a'"
                      >{{ area }}
                    </v-chip>
                  </v-chip-group>
                </v-col>
              </v-row>
              <v-row class="pa-0">
                <v-col cols="6">
                  {{ article.period }}박 {{ article.period + 1 }}일
                </v-col>
              </v-row>
              <v-row class="pa-0">
                <v-col>
                  {{ article.name }} ({{ article.regDate.split("T")[0] }}
                  {{ article.regDate.split("T")[1].split(".")[0] }})
                </v-col>
              </v-row>
            </v-col>
          </v-row> -->
          </v-card>
        </v-col>
      </v-row>
      <v-row style="position: absolute; right: 0">
        <v-btn class="mx-16" outlined color="#4A8072" @click="goToNewArticle()"
          >게시글 작성</v-btn
        >
      </v-row>
    </v-container>
    <v-spacer></v-spacer>
    <br />
    <v-row justify="center" class="pagenation-bar">
      <v-pagination
        v-model="currentPage"
        class="my-4"
        :length="totalPage"
        color="#4a8072"
      ></v-pagination>
    </v-row>
  </div>
</template>

<script>
import API from "@/api/RESTAPI";
import SearchArticle from "@/components/community/SearchArticle.vue";
import LoginButton from "@/components/KakaoLogin/LoginButton.vue";
const api = API;
export default {
  name: "CommunityView",
  components: {
    SearchArticle,
    LoginButton,
  },
  data() {
    return {
      isSearchByTitle: true,
      currentPage: 1,
      searchData: "",
      articleList: [],
      totalPage: 0,
      token: this.$cookies.get("refresh"),
      alert: false,
    };
  },
  async created() {
    await this.getArticleListByTitle(1);
  },
  watch: {
    currentPage(newVal) {
      if (this.isSearchByTitle) {
        this.getArticleListByTitle(newVal);
      } else {
        this.getArticleListByArea(newVal);
      }
    },
  },
  methods: {
    async getArticleListByTitle(page) {
      this.res = await api.getArticleListByTitle(this.searchData, page - 1);
      this.articleList = this.res.searchList.articleList;
      this.totalPage = this.res.searchList.totalPage;
    },
    async getArticleListByArea(page) {
      this.res = await api.getArticleListByArea(this.searchData, page - 1);
      this.articleList = this.res.articleList;
    },
    async getArticleListByAreaLength() {
      this.res = await api.getArticleListByAreaLength(this.searchData);
      this.totalPage = this.res.length;
    },
    goToArticle(articleId) {
      this.$router.push("/article/" + articleId);
    },
    async goSearch(val, data) {
      this.searchData = data;
      if (val == 1) {
        this.isSearchByTitle = true;
        this.getArticleListByTitle(1);
      } else {
        this.isSearchByTitle = false;
        this.getArticleListByAreaLength();
        this.getArticleListByArea(1);
      }
    },
    goToNewArticle() {
      if (this.$cookies.get("refresh") == null) {
        this.alert = !this.alert;
      } else {
        this.$router.push("/newArticle");
      }
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
.made-container {
  padding-top: 6px !important;
  height: 100%;
  position: relative;
}

.textCutting {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 80%;
}

/* .img-avatar {
  height: 100% !important;
  width: 40% !important;
} */
/* .v-chip-group .v-slide-group__content {
  padding: 0 0;
}
.v-chip-group .v-chip {
  margin-top: 0;
  margin-bottom: 0;
} */
</style>
