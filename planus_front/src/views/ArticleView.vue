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
    <article-detail :articleId="articleId"></article-detail>
    <comment-list :articleId="articleId"></comment-list>
  </div>
</template>

<script>
import CommentList from "@/components/community/CommentList.vue";
import ArticleDetail from "@/components/community/ArticleDetail.vue";
import LoginButton from "@/components/KakaoLogin/LoginButton.vue";
export default {
  name: "CommunityView",
  components: {
    CommentList,
    ArticleDetail,
    LoginButton,
  },
  data() {
    return {
      articleId: 0,
    };
  },
  created() {
    this.articleId = Number(this.$route.params.articleId);
  },
  methods: {
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

<style></style>
