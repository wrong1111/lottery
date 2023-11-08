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
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入用户名"
          clearable
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
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="el-icon-circle-plus-outline"
          size="mini"
          @click="newSysUser"
          plain
          >添加</el-button
        >
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="voList" border stripe>
      <el-table-column label="用户ID" align="center" prop="id" width="100" fixed />
      <el-table-column
        label="用户名"
        align="center"
        prop="username"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column
        label="昵称"
        align="center"
        prop="name"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column label="支付密码" align="center" width="100">
        <template slot-scope="scope">
          <span>******</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="danger" @click="deleteUser(scope.row)"
            >删除</el-button
          >
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

    <add-sys-user-dialog
      title="添加用户"
      :visible.sync="dialogVisible"
      @confirm="addUserReq"
    ></add-sys-user-dialog>
  </div>
</template>

<script>
import { addSysUser, getSysList, deleteSysUser } from "@/api/client";
import AddSysUserDialog from "./components/AddSysUserDialog.vue";
import { Message, MessageBox } from "element-ui";

export default {
  name: "ClientSystem",
  components: {
    AddSysUserDialog,
  },
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
        username: undefined,
        name: undefined,
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
      getSysList(this.queryParams).then((response) => {
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
    // 新增用户
    newSysUser() {
      this.dialogVisible = true;
    },

    // 新增用户请求
    addUserReq(data) {
      addSysUser(data).then((response) => {
        if (!response.errorCode) {
          this.getList();
        }
      });
    },

    // 删除用户
    deleteUser(row) {
      MessageBox.confirm("删除该用户将会清理所有相关联的数据,确定要进行操作吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          deleteSysUser(row.id).then((response) => {
            if (!response.errorCode) {
              Message({
                type: "success",
                message: "删除成功",
              });
              this.getList();
            }
          });
        })
        .catch(() => {});
    },
  },
};
</script>

<style scoped lang="scss"></style>
