<template>
  <div>
    <v-spacer />
    <v-row align="center" justify="center">
      <v-col cols="2">
        <v-select
          v-model="select"
          :items="items"
          item-text="kind"
          item-value="val"
          return-object
          single-line
          color="#4a8072"
          item-color="green"
        ></v-select>
      </v-col>
      <v-col cols="5">
        <v-text-field
          v-model="keword"
          placeholder="검색어를 입력하세요"
          v-if="select.val == 1"
        ></v-text-field>
        <v-menu
          v-else
          ref="areaMenu"
          v-model="areaMenu"
          :close-on-content-click="false"
          :return-value.sync="areas"
          transition="scale-transition"
          offset-y
          min-width="auto"
        >
          <template v-slot:activator="{ on, attrs }">
            <v-combobox
              v-model="areas"
              label="여행지를 선택하세요"
              allow-overflow
              multiple
              chips
              dense
              solo
              clearable
              readonly
              appendIcon=""
              clear-icon="mdi-close-circle"
              v-bind="attrs"
              v-on="on"
              @click:clear="clearArea"
            >
            </v-combobox>
          </template>
          <v-card v-model="areaMenu" style="width: 700px">
            <v-card-title>
              <v-tabs v-model="tabs" fixed-tabs>
                <v-tab style="padding: 0">서울/인천</v-tab>
                <v-tab>경기도</v-tab>
                <v-tab>강원도</v-tab>
                <v-tab>충청도</v-tab>
                <v-tab>경상도</v-tab>
                <v-tab>전라도</v-tab>
                <v-tab>제주도</v-tab>
              </v-tabs>
            </v-card-title>
            <v-card-text
              ><v-tabs-items v-model="tabs"
                ><v-tab-item>
                  <v-chip-group active-class="primary--text" column>
                    <v-chip
                      v-for="(area, i) in areaGroup1"
                      :key="i"
                      :area="area"
                      @click="addArea(area)"
                    >
                      {{ area.siName }}
                    </v-chip>
                  </v-chip-group>
                </v-tab-item>
                <v-tab-item>
                  <v-chip-group active-class="primary--text" column>
                    <v-chip
                      v-for="(area, i) in areaGroup2"
                      :key="i"
                      :area="area"
                      @click="addArea(area)"
                    >
                      {{ area.siName }}
                    </v-chip>
                  </v-chip-group>
                </v-tab-item>
                <v-tab-item>
                  <v-chip-group active-class="primary--text" column>
                    <v-chip
                      v-for="(area, i) in areaGroup3"
                      :key="i"
                      :area="area"
                      @click="addArea(area)"
                    >
                      {{ area.siName }}
                    </v-chip>
                  </v-chip-group>
                </v-tab-item>
                <v-tab-item>
                  <v-chip-group active-class="primary--text" column>
                    <v-chip
                      v-for="(area, i) in areaGroup4"
                      :key="i"
                      :area="area"
                      @click="addArea(area)"
                    >
                      {{ area.siName }}
                    </v-chip>
                  </v-chip-group>
                </v-tab-item>
                <v-tab-item>
                  <v-chip-group active-class="primary--text" column>
                    <v-chip
                      v-for="(area, i) in areaGroup5"
                      :key="i"
                      :area="area"
                      @click="addArea(area)"
                    >
                      {{ area.siName }}
                    </v-chip>
                  </v-chip-group>
                </v-tab-item>
                <v-tab-item>
                  <v-chip-group active-class="primary--text" column>
                    <v-chip
                      v-for="(area, i) in areaGroup6"
                      :key="i"
                      :area="area"
                      @click="addArea(area)"
                    >
                      {{ area.siName }}
                    </v-chip>
                  </v-chip-group>
                </v-tab-item>
                <v-tab-item>
                  <v-chip-group active-class="primary--text" column>
                    <v-chip
                      v-for="(area, i) in areaGroup7"
                      :key="i"
                      :area="area"
                      @click="addArea(area)"
                    >
                      {{ area.siName }}
                    </v-chip>
                  </v-chip-group>
                </v-tab-item>
              </v-tabs-items>
            </v-card-text>
          </v-card>
        </v-menu>
      </v-col>
      <v-col cols="1">
        <v-btn @click="goSearch(select.val)">검색</v-btn>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import API from "@/api/RESTAPI";
const api = API;

export default {
  data() {
    return {
      select: { kind: "제목", val: 1 },
      items: [
        { kind: "제목", val: 1 },
        { kind: "지역", val: 2 },
      ],
      keword: "",
      areaId: [],
      areaGroup1: [],
      areaGroup2: [],
      areaGroup3: [],
      areaGroup4: [],
      areaGroup5: [],
      areaGroup6: [],
      areaGroup7: [],
      tabs: 0,
      areas: [],
      areaMenu: false,
    };
  },
  created() {
    this.getAllArea();
  },
  methods: {
    async getAllArea() {
      this.res = await api.getAllArea();
      let res = this.res.areaList;
      this.areaGroup1 = res.slice(0, 2);
      this.areaGroup2 = res.slice(2, 33);
      this.areaGroup3 = res.slice(33, 51);
      this.areaGroup4 = res.slice(51, 79);
      this.areaGroup5 = res.slice(79, 123);
      this.areaGroup6 = res.slice(123, 160);
      this.areaGroup7 = res.slice(160);
    },
    addArea(area) {
      if (!this.areaId.includes(area.areaId)) {
        this.areaId.push(area.areaId);
        this.areas.push(area.siName);
      }
    },
    clearArea() {
      if (!this.areaMenu) {
        this.areaId = [];
        this.areas = [];
      }
    },
    goSearch(val) {
      if (val == 1) {
        this.$emit("goSearch", val, this.keword);
      } else {
        this.$emit("goSearch", val, this.areaId);
      }
    },
  },
};
</script>

<style></style>
