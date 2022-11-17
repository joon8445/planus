<template>
  <div></div>
</template>

<script>
export default {
  name: "getKakaoToken",
  data() {
    return {};
  },
  created() {
    const token = this.$route.query.token;
    const refreshToken = this.$route.query.refresh;
    this.saveToken(token, refreshToken);
  },
  methods: {
    saveToken(token, refreshToken) {
      this.$cookies.set("token", token, 60 * 30);
      this.$cookies.set("refresh", refreshToken, 2 * 60 * 60 * 24 * 30);
      this.$router.push("/");
    },
  },
};
</script>

<style></style>
