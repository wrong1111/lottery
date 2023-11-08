<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="voList" border stripe>
      <el-table-column label="id" align="center" prop="id" width="100" fixed />
      <el-table-column
        label="名称"
        align="center"
        prop="name"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column
        label="LOGO"
        align="center"
        prop="url"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="上下线状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag
            size="mini"
            effect="plain"
            :type="scope.row.line === '1' ? 'danger' : 'success'"
            >{{ scope.row.line === "1" ? "下架" : "上架" }}</el-tag
          >
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="修改时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="success" plain @click="putOnItem(scope.row)"
            >上架</el-button
          >
          <el-button size="mini" type="danger" plain @click="takeOffItem(scope.row)"
            >下架</el-button
          >
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getBallList, updateBallLine } from "@/api/ballgame";

export default {
  name: "BallgameSet",
  components: {},
  props: {},
  data() {
    return {
      // 遮罩层
      loading: false,
      // 数据
      voList: [],
    };
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {
    this.getList();
  },
  methods: {
    // 获取统计列表
    getList() {
      this.loading = true;
      getBallList().then((response) => {
        this.loading = false;

        if (!response.errorCode) {
          this.voList = response.voList;
        }
      });
    },
    // 上架
    putOnItem(row) {
      updateBallLine(row.id, "0").then((response) => {
        if (!response.errorCode) {
          this.getList();
        }
      });
    },
    // 下架
    takeOffItem(row) {
      updateBallLine(row.id, "1").then((response) => {
        if (!response.errorCode) {
          this.getList();
        }
      });
    },
  },
};
</script>

<style scoped lang="scss"></style>
