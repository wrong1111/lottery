<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      :inline="true"
      v-show="showSearch"
      size="mini"
    >
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          placeholder="请输入昵称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="voList" border stripe>
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column
        label="手机号"
        align="center"
        prop="phone"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="昵称"
        align="center"
        prop="nickname"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="操作方式"
        align="center"
        prop="descriptor"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="操作时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { getLogList } from "@/api/log";

export default {
  name: "Log",
  components: {},
  props: {},
  data() {
    return {
      // 遮罩层
      loading: false,
      // 总数据条数
      total: 0,
      // 列表数据
      voList: [],
      // 查询参数
      queryParams: {
        nickname: undefined,
        phone: undefined,
        pageNo: 1,
        pageSize: 10,
      },
      // 是否显示搜索
      showSearch: true,
      // 弹窗添加用户
      dialogVisible: false,
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
      getLogList(this.queryParams).then((response) => {
        this.loading = false;
        if (!response.errorCode) {
          this.total = response.total;
          this.voList = response.voList;
        }
      });
    },
    // 查询数据
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    // 重置数据
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
  },
};
</script>

<style scoped lang="scss"></style>
