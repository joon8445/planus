<template>
  <v-container class="made-container">
    <v-row justify="center" height="75vh" class="mt-0">
      <v-simple-table>
        <template v-slot:default>
          <thead>
            <tr>
              <th class="text-left">여행날짜</th>
              <th class="text-left">인원,지역</th>
              <th class="text-left" style="width: 35vw">제목</th>
              <th class="text-left">좋아요</th>
              <th class="text-left">조회수</th>
              <th class="text-left">등록(수정)시간</th>
            </tr>
          </thead>
          <tbody>
            <tr
              style="cursor: pointer"
              v-for="article in articleList"
              :key="article.articleId"
              @click="goToArticle(article.articleId)"
            >
              <td class="textCutting">
                {{ article.trip.startDate }} <br />
                {{ "~" + article.trip.endDate }}
              </td>
              <td class="textCutting">
                {{ article.trip.participants + "명" }}<br />
                {{ article.trip.areaList + "" }}
              </td>
              <td class="textCutting">
                {{ article.title }}
              </td>
              <td>{{ article.likeCount }}</td>
              <td>{{ article.hits }}</td>
              <td class="textCutting">
                {{ article.regDate.split("T")[0] }}<br />{{
                  article.regDate.split("T")[1].split(".")[0]
                }}
              </td>
            </tr>
          </tbody>
        </template>
      </v-simple-table>
    </v-row>
    <v-spacer />
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
  data() {
    return {
      articleList: [],
      currentPage: 1,
      totalPage: 1,
    };
  },
  async created() {
    await this.getLikeList();
  },
  watch: {
    currentPage() {
      this.getLikeList();
    },
  },
  methods: {
    async getLikeList() {
      await api.getMyLikedArticle(this.currentPage - 1).then((res) => {
        this.articleList = res.articleList;
        this.totalPage = res.totalPage;
      });
    },
    goToArticle(articleId) {
      this.$router.push("/article/" + articleId);
    },
  },
};
</script>

<style></style>
