import axios from "axios";
import VueCookies from "vue-cookies";
import jwt_decode from "jwt-decode";

const baseURL = process.env.VUE_APP_API_URL;

const baseAxios = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

function needRefresh() {
  var token = VueCookies.get("token");
  if (token == null || jwt_decode(token).exp - Date.now() / 1000 < 180) {
    return true;
  }
  return false;
}

baseAxios.interceptors.request.use(async (request) => {
  if (!needRefresh()) {
    request.headers.Authorization = "Bearer " + VueCookies.get("token");
  } else {
    if (VueCookies.get("refresh") != null) {
      var temp = axios.create();
      await temp
        .patch(baseURL + "/login", { refreshToken: VueCookies.get("refresh") })
        .then((res) => {
          VueCookies.set("token", res.data.newToken, 60 * 30);
          request.headers.Authorization = "Bearer " + VueCookies.get("token");
        })
        .catch((error) => {
          if (error.response.data.status == 403) {
            VueCookies.remove("refresh");
            window.location.href = "/";
          } else {
            console.log(error);
          }
        });
    }
  }
  return request;
});
baseAxios.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.log(error);
    if (error.response.data.status == 403) {
      window.location.href = "/";
    }
  }
);

const API = {
  instance: baseAxios,
  async getAllArea() {
    const response = await this.instance.get("/trip/area");
    return response.data;
  },
  async getBestArea() {
    const response = await this.instance.get("/trip/bestarea");
    return response.data;
  },
  async getFestival(page) {
    const response = await this.instance.get("/trip/festival", {
      params: { page },
    });
    return response.data;
  },
  async createTrip(startDate, period, areaId) {
    const response = await this.instance.post("/trip", {
      startDate: startDate,
      period: period,
      areaId: areaId,
    });
    return response.data;
  },
  async getTripInfo(tripUrl) {
    const response = await this.instance.get("/trip" + "?tripUrl=" + tripUrl);
    return response.data;
  },
  async addMember(tripId) {
    const response = await this.instance.post(
      "/trip/member" + "?tripId=" + tripId
    );
    return response.data;
  },
  async getMemberList(tripId) {
    const response = await this.instance.get(
      "/trip/member" + "?tripId=" + tripId
    );
    return response.data;
  },
  async getRecommend(lat, lng, page, size) {
    const response = await this.instance.get("/recommend", {
      params: { lat: lat, lng: lng, page: page, size: size },
    });
    return response.data;
  },
  async getRecommendPageLength(lat, lng, size) {
    const response = await this.instance.get("/recommend/length", {
      params: { lat: lat, lng: lng, size: size },
    });
    return response.data;
  },
  async signOut() {
    const response = await this.instance.get("/login/signout");
    return response.data;
  },
  async getMyInfo() {
    const response = await this.instance.get("/mypage");
    return response.data.result;
  },
  async changeMyInfo(newNickname) {
    const response = await this.instance.put("/mypage", {
      nickname: newNickname,
    });
    return response.data;
  },
  async getBucketList(tripId) {
    const response = await this.instance.get("/buckets/" + tripId);
    return response.data;
  },
  async saveBucketList(tripId) {
    const response = await this.instance.post("/buckets/" + tripId);
    return response.data;
  },
  async getMyTripMade(page) {
    const response = await this.instance.get("/mytrip/made", {
      params: { page },
    });
    return response.data;
  },
  async getMyTripShared(page) {
    const response = await this.instance.get("/mytrip/shared", {
      params: { page },
    });
    return response.data;
  },
  async getPlanId(tripId) {
    const response = await this.instance.get("/plans/" + tripId);
    return response.data;
  },
  async getPlanList(planIdList) {
    const response = await this.instance.post("/plans/planlist", planIdList);
    return response.data;
  },
  //안 써서 지워야 할 수도
  async savePlanStart(planId) {
    const response = await this.instance.get("/plans/start/" + planId);
    return response.data;
  },
  async savePlan(tripId, planIdList, complete) {
    const response = await this.instance.post("/plans", {
      tripId: tripId,
      planIdList: planIdList,
      complete: complete,
    });
    return response.data;
  },
  async deleteTrip(tripId) {
    const response = await this.instance.delete("/mytrip", {
      params: { tripId },
    });
    return response.data;
  },
  async addComment(id, content) {
    const response = await this.instance.post("/comment", {
      id: id,
      content: content,
    });
    return response.data;
  },
  async modifyComment(id, content) {
    const response = await this.instance.put("/comment", {
      id: id,
      content: content,
    });
    return response.data;
  },
  async getArticleComment(articleId, page) {
    const response = await this.instance.get(
      "/comment" + "?articleId=" + articleId,
      {
        params: { page },
      }
    );
    return response.data;
  },
  async getMyComment(page) {
    const response = await this.instance.get("/mypage/comment", {
      params: { page },
    });
    return response.data;
  },
  async delComment(commentId) {
    const response = await this.instance.delete(
      "/comment" + "?commentId=" + commentId
    );
    return response.data;
  },
  async getArticleListByTitle(title, page) {
    const response = await this.instance.get(
      "/articles/title" + "?title=" + title,
      {
        params: { page },
      }
    );
    return response.data;
  },
  async getArticleListByArea(area, page) {
    const response = await this.instance.get(
      "/articles/area" + "?area=" + area,
      {
        params: { page },
      }
    );
    return response.data;
  },
  async getArticleListByAreaLength(area) {
    const response = await this.instance.get(
      "/articles/area/length" + "?area=" + area
    );
    return response.data;
  },
  async logout() {
    const response = await this.instance
      .get("/login/logout")
      .catch((error) => console.log(error));
    return response;
  },
  async dummy() {
    const response = await this.instance.get("/login/dummy");
    return response;
  },

  async googleApi(url) {
    const response = await this.instance.post("/google", url);
    return response.data;
  },

  async getArticle(articleId) {
    const response = await this.instance.get("/articles/" + articleId);
    return response.data;
  },

  async createArticle(title, content, tripId) {
    const response = await this.instance.post("/articles", {
      title: title,
      content: content,
      tripId: tripId,
    });
    return response.data;
  },

  async updateArticle(articleId, title, content, tripId) {
    await this.instance.put("/articles/" + articleId, {
      title: title,
      content: content,
      tripId: tripId,
    });
  },

  async deleteArticle(articleId) {
    await this.instance.delete("/articles/" + articleId);
  },

  async likeArticle(articleId) {
    const response = await this.instance.post("/articles/like/" + articleId);
    return response.data;
  },

  async getMyArticle(page) {
    const response = await this.instance.get("/mypage/articles", {
      params: { page },
    });
    return response.data;
  },

  async getMyLikedArticle(page) {
    const response = await this.instance.get("/mypage/articles/like", {
      params: { page },
    });
    return response.data;
  },
  async getMyAllTrip() {
    const response = await this.instance.get("/mytrip/all");
    return response.data;
  },

  async getComplete(tripUrl) {
    const response = await this.instance.get("/complete/" + tripUrl);
    return response.data;
  },
  async copyTrip(tripId, startDate) {
    const response = await this.instance.post("/complete/copy", {
      tripId,
      startDate,
    });
    return response.data;
  },
  async getTimetableListForMap(planId) {
    const response = await this.instance.get("/plans/map/" + planId);
    return response.data;
  },
};

export default API;
