import Vue from "vue";
import VueRouter from "vue-router";
import HomeView from "../views/HomeView.vue";
import MypageView from "../views/MypageView.vue";
import PlanView from "../views/PlansView.vue";
import getKakaoToken from "../components/KakaoLogin/GetKakaoToken.vue";
import CompleteView from "../views/CompleteView.vue";
import CommunityView from "../views/CommunityView.vue";
import ArticleView from "../views/ArticleView.vue";
import ArticleNewView from "@/views/ArticleNewView";
import ArticleEditView from "@/views/ArticleEditView";

Vue.use(VueRouter);

const routes = [
  { path: "/", name: "homeview", component: HomeView },
  {
    path: "/mypage",
    name: "mypage",
    component: MypageView,
    redirect: "/mypage/myinfo",
    children: [
      {
        path: "myinfo",
        name: "myinfo",
        component: () => import("@/components/mypage/MyInfoFix.vue"),
      },
      {
        path: "mytrip",
        name: "mytrip",
        component: () => import("@/components/mypage/MadeTripList.vue"),
      },
      {
        path: "sharedtrip",
        name: "sharedtrip",
        component: () => import("@/components/mypage/SharedTripList.vue"),
      },
      {
        path: "myarticle",
        name: "myarticle",
        component: () => import("@/components/mypage/MyArticleList.vue"),
      },
      {
        path: "mylike",
        name: "mylike",
        component: () => import("@/components/mypage/MyLikeList.vue"),
      },
      {
        path: "mycomment",
        name: "mycomment",
        component: () => import("@/components/mypage/MyCommentList.vue"),
      },
    ],
  },
  {
    path: "/plans/:tripUrl",
    name: "plans",
    component: PlanView,
  },
  {
    path: "/login/getkakaotoken",
    name: "getKakaoToken",
    component: getKakaoToken,
  },
  {
    path: "/complete/:tripUrl",
    name: "complete",
    component: CompleteView,
  },
  {
    path: "/community",
    name: "community",
    component: CommunityView,
  },

  {
    path: "/article/:articleId",
    name: "article",
    component: ArticleView,
  },
  {
    path: "/newArticle",
    name: "newArticle",
    component: ArticleNewView,
  },
  {
    path: "/editArticle/:articleId",
    name: "editArticle",
    component: ArticleEditView,
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
