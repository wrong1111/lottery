<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      :inline="true"
      v-show="showSearch"
      size="mini"
    >
      <el-form-item label="店铺名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入店铺名"
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
          @click="addNewShop"
          plain
          >添加加盟店</el-button
        >
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="voList" border>
      <el-table-column label="ID" align="center" prop="id" width="100" />
      <el-table-column
        label="店铺名"
        align="center"
        prop="name"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="LOGO" align="center" width="100">
        <template slot-scope="scope">
          <el-image
            style="width: 60px; height: 60px"
            :src="scope.row.logo"
            fit="cover"
          ></el-image>
        </template>
      </el-table-column>
      <el-table-column
        label="店铺余额"
        align="center"
        prop="balance"
        :show-overflow-tooltip="true"
        width="120"
      />
      <el-table-column label="上/下架" align="center" width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.line === "1" ? "下架" : "上架" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="400">
        <template slot-scope="scope">
          <el-button size="mini" type="danger" plain @click="upShop(scope.row)"
            >上架</el-button
          >
          <el-button size="mini" type="primary" plain @click="downShop(scope.row)"
            >下架</el-button
          >
          <el-button size="mini" type="success" plain @click="configPayAction(scope.row)"
            >支付配置</el-button
          >
          <el-button size="mini" type="warning" plain @click="recharegeShop(scope.row)"
            >充值</el-button
          >
          <el-button size="mini" type="danger" plain @click="deleteShopAction(scope.row)"
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

    <AddShopDialog
      title="添加加盟店"
      :visible.sync="dialogAddVisible"
      @confirm="addShopReq"
    ></AddShopDialog>

    <ShopRechargeDialog
      title="店铺充值"
      :name="this.updateShop.name"
      :visible.sync="dialogRechargeVisible"
      @confirm="rechargeReq"
    ></ShopRechargeDialog>

    <ShopPayConfigDialog
      :v-if="dialogConfigVisible"
      title="阿里支付配置"
      :id="this.updateShop.id"
      :visible.sync="dialogConfigVisible"
      @confirm="configPayReq"
    >
    </ShopPayConfigDialog>
  </div>
</template>

<script>
import {
  getShopList,
  addShop,
  deleteShop,
  updateShopPay,
  updateShopLine,
  rechargeShop,
} from "@/api/shop";
import { deepClone } from "@/utils";
import { Message, MessageBox } from "element-ui";
import AddShopDialog from "./components/AddShopDialog.vue";
import ShopRechargeDialog from "./components/ShopRechargeDialog.vue";
import ShopPayConfigDialog from "./components/ShopPayConfigDialog.vue";

export default {
  name: "ShopList",
  components: {
    AddShopDialog,
    ShopRechargeDialog,
    ShopPayConfigDialog,
  },
  props: {},
  data() {
    return {
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 用户列表
      voList: [],
      // 遮罩层
      loading: true,
      // 用户查询
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: undefined,
      },
      // 添加用户弹窗
      dialogAddVisible: false,
      // 支付配置
      dialogConfigVisible: false,
      // 充值弹窗
      dialogRechargeVisible: false,
      // 当前需要修改的用户
      updateShop: {},
    };
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {
    this.getList();
  },
  methods: {
    // 获取用户列表
    getList() {
      this.loading = true;
      getShopList(this.queryParams).then((response) => {
        this.loading = false;
        this.total = response.total;
        this.voList = response.voList;
      });
    },
    // 条件查询
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    // 重置查询条件
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // ---------------- 用户相关操作 -----------------------
    // 新增用户
    addNewShop() {
      this.dialogAddVisible = true;
    },

    // 发送新增店铺请求
    addShopReq(data) {
      addShop(data).then((response) => {
        if (!response.errorCode) {
          this.getList();
        }
      });
    },

    // 店铺上架
    upShop(row) {
      this.upOrDownShop(row.id, "0");
    },

    // 店铺下架
    downShop(row) {
      this.upOrDownShop(row.id, "1");
    },

    // 店铺上/下架
    upOrDownShop(shopId, line) {
      updateShopLine(shopId, line).then((response) => {
        if (!response.errorCode) {
          Message({
            type: "success",
            message: "操作成功!",
          });
          this.getList();
        }
      });
    },
    // 支付配置
    configPayAction(row) {
      this.updateShop = deepClone(row);
      this.dialogConfigVisible = true;
    },

    // 支付配置请求
    configPayReq(form) {
      let data = { ...form };
      data.tenantId = this.updateShop.id;
      updateShopPay(data).then((response) => {
        if (!response.errorCode) {
          Message({
            message: "配置成功",
            type: "success",
          });
        }
      });
    },

    // 删除用户
    deleteShopAction(row) {
      MessageBox.confirm("删除该店铺将会清理所有相关联的数据,确定要进行操作吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          deleteShop(row.id).then((response) => {
            if (!response.errorCode) {
              Message({
                type: "success",
                message: "删除成功!",
              });
              this.getList();
            }
          });
        })
        .catch(() => {});
    },

    // 充值
    recharegeShop(row) {
      this.updateShop = deepClone(row);
      this.dialogRechargeVisible = true;
    },

    // 充值请求
    rechargeReq(form) {
      let data = { ...form };
      data.id = this.updateShop.id;
      rechargeShop(data).then((response) => {
        if (!response.errorCode) {
          this.getList();
        }
      });
    },
  },
};
</script>

<style scoped lang="scss"></style>
