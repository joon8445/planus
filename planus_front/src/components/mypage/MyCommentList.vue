<template>
  <v-container class="made-container">
    <v-row justify="center" height="75vh" class="mt-0">
      <v-simple-table>
        <template v-slot:default>
          <thead>
            <tr>
              <th class="text-left" style="width: 20vw">게시글 제목</th>
              <th class="text-left" style="width: 45vw">댓글</th>
              <th class="text-left">등록(수정)시간</th>
              <th class="text-left" style="width: 60px">삭제</th>
            </tr>
          </thead>
          <tbody>
            <tr
              style="cursor: pointer"
              v-for="comment in commentList"
              :key="comment.commentId"
              @click="goToArticle(comment.articleId)"
            >
              <td class="textCutting">{{ comment.title }}</td>
              <td class="textCutting">{{ comment.content }}</td>
              <td class="textCutting">
                {{ comment.regDate.split("T")[0] }}<br />{{
                  comment.regDate.split("T")[1].split(".")[0]
                }}
              </td>
              <td class="pa-0 align-center justify-center">
                <comment-delete
                  :commentId="comment.commentId"
                  @deleteComment="delComment"
                ></comment-delete>
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
import CommentDelete from "@/components/community/CommentDelete.vue";
import API from "@/api/RESTAPI";
const api = API;
export default {
  components: {
    CommentDelete,
  },
  data() {
    return {
      commentList: [],
      currentPage: 1,
      totalPage: 1,
    };
  },
  async created() {
    await this.getMyCommentList();
  },
  watch: {
    currentPage() {
      this.getMyCommentList();
    },
  },
  methods: {
    async getMyCommentList() {
      await api.getMyComment(this.currentPage - 1).then((res) => {
        this.commentList = res.commentPage.commentList;
        this.totalPage = res.commentPage.totalPage;
      });
    },
    goToArticle(articleId) {
      this.$router.push("/article/" + articleId);
    },
    async delComment(commentId) {
      await api.delComment(commentId);
      this.currentPage = 1;
      this.getMyCommentList();
    },
  },
};
</script>

<style></style>
