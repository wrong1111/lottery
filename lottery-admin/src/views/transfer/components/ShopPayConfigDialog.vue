<template>
  <div>
    <el-dialog
      v-bind="$attrs"
      v-on="$listeners"
      width="680px"
      @open="onOpen"
      @close="onClose"
    >
      <el-form
        :model="form"
        ref="shopPayConfigForm"
        :rules="rules"
        size="medium"
        label-width="80px"
      >
        <el-form-item label="AppId" prop="aliPayAppId">
          <el-input v-model="form.aliPayAppId" placeholder="请输入AppId"></el-input>
        </el-form-item>

        <el-form-item label="公钥" prop="alipayPublicKey">
          <el-input v-model="form.alipayPublicKey" placeholder="请输入公钥" />
        </el-form-item>

        <el-form-item label="私钥" prop="alipayPrivateKey">
          <el-input v-model="form.alipayPrivateKey" placeholder="请输入私钥" />
        </el-form-item>
      </el-form>

      <el-row :gutter="10" type="flex" justify="end">
        <el-col :span="1.5">
          <el-button @click="close">取 消</el-button>
          <el-button type="primary" @click="handleConfirm">确 定</el-button>
        </el-col>
      </el-row>

      <el-table :v-loading="payLoading" :data="payList" border class="mt10">
        <el-table-column prop="dictDesc" label="描述"> </el-table-column>
        <el-table-column prop="value" label="数据值" show-overflow-tooltip>
        </el-table-column>
      </el-table>

      <el-table :v-loading="userLoading" :data="userList" border class="mt10">
        <el-table-column prop="name" label="账户类型"> </el-table-column>
        <el-table-column prop="username" label="用户名"> </el-table-column>
        <el-table-column prop="password" label="密码" show-overflow-tooltip>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { getAliPayConfig, getUserList } from "@/api/shop";

export default {
  name: "ShopPayConfigDialog",
  components: {},
  props: {
    id: {
      type: Number,
    },
  },
  data() {
    return {
      form: {
        aliPayAppId: "",
        alipayPublicKey: "",
        alipayPrivateKey: "",
      },
      rules: {
        aliPayAppId: [
          {
            required: true,
            message: "AppId不能为空",
            trigger: "blur",
          },
        ],
        alipayPublicKey: [
          {
            required: true,
            message: "公钥不能为空",
            trigger: "blur",
          },
        ],
        alipayPrivateKey: [
          {
            required: true,
            message: "私钥不能为空",
            trigger: "blur",
          },
        ],
      },
      // 支付数据
      payList: [],
      payLoading: false,
      // 用户数据
      userList: [],
      userLoading: false,
    };
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {
    this.getPayConfigListReq();
    this.getUserListReq();
  },
  methods: {
    // 弹窗打开
    onOpen() {
      if (this.id) {
        this.getPayConfigListReq();
        this.getUserListReq();
      }
    },
    // 弹窗关闭
    onClose() {},
    // 主动点击取消
    close() {
      this.resetForm("shopPayConfigForm");
      this.$emit("update:visible", false);
    },
    // 主动点击确定
    handleConfirm() {
      this.$refs.shopPayConfigForm.validate((valid) => {
        if (!valid) return;
        this.$emit("confirm", { ...this.form });
        this.close();
      });
    },
    // 获取用户列表
    getPayConfigListReq() {
      this.payLoading = true;
      getAliPayConfig(this.id).then((response) => {
        this.payLoading = false;
        this.payList = response.voList;
      });
    },
    // 获取用户列表
    getUserListReq() {
      this.userLoading = true;
      getUserList(this.id).then((response) => {
        this.userLoading = false;
        this.userList = response.voList;
      });
    },
  },
};
</script>

<style scoped lang="scss"></style>
