<template>
  <v-col cols="3" class="px-0">
    <v-row class="px-0 mx-0">
      <v-col class="px-0 mx-0">
        <v-card outlined>
          <v-card-subtitle class="py-0 font-weight-black d-flex justify-center">
            {{ timeTable.place }}
          </v-card-subtitle>
          <v-card-text class="py-0 text-caption d-flex justify-center"
            >{{
              parseInt(startTime / 60)
                .toString()
                .padStart(2, 0)
            }}:{{ (startTime % 60).toString().padStart(2, 0) }} ~
            {{
              parseInt(endTime / 60)
                .toString()
                .padStart(2, 0)
            }}:{{ (endTime % 60).toString().padStart(2, 0) }}</v-card-text
          >
        </v-card>
      </v-col>
      <v-col class="px-0">
        <v-card flat v-if="!isFinish">
          <v-card-subtitle class="py-0 font-weight-black d-flex justify-center">
            <span v-if="timeTable.transit == 'BUS'">{{ move.BUS }}</span>
            <span v-else-if="timeTable.transit == 'CAR'">{{ move.CAR }}</span>
            <span v-else>{{ move.NONE }}</span>
          </v-card-subtitle>
          <v-card-text class="py-0 text-caption d-flex justify-center">{{
            timeTable.moveTime
          }}</v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-col>
</template>

<script>
export default {
  name: "TimeTableCard",
  props: {
    timeTable: Object,
    startTime: Number,
    endTime: Number,
    isFinish: Boolean,
  },
  data() {
    return {
      move: {
        BUS: "대중교통",
        CAR: "자가용",
        NONE: "-",
      },
    };
  },
};
</script>

<style scoped>
.theme--light.v-card > .v-card__subtitle {
  color: #544c4c;
}
.theme--light.v-sheet--outlined {
  border: solid 1px #4a8072;
}
</style>
