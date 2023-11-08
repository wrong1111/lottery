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
        ref="addShopForm"
        :rules="rules"
        size="medium"
        label-width="100px"
      >
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="店名" prop="name">
              <el-input v-model="form.name" placeholder="请输入店名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="余额" prop="balance">
              <el-input
                v-model="form.balance"
                placeholder="请输入余额"
                oninput="this.value = this.value.replace(/[^0-9]/g, '');"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="logo" prop="logo">
              <image-upload
                :multiple="true"
                :limit="1"
                @input="uploadPicture"
              ></image-upload>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="后台用户名" prop="sysUsername">
              <el-input
                v-model="form.sysUsername"
                placeholder="请输入后台用户名"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="后台密码" prop="sysPassword">
              <el-input
                v-model="form.sysPassword"
                placeholder="请输入后台密码"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付密码" prop="payPwd">
              <el-input
                v-model="form.payPwd"
                placeholder="请输入支付密码"
                oninput="this.value = this.value.replace(/[^0-9]/g, '');"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="APP手机号" prop="appPhone">
              <el-input v-model="form.appPhone" placeholder="请输入手机号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="APP密码" prop="appPassword">
              <el-input v-model="form.appPassword" placeholder="请输入密码"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div slot="footer">
        <el-button @click="close">取 消</el-button>
        <el-button type="primary" @click="handleConfirm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { validAlphabets, validPayPwd } from "@/utils/validate";
import { Message } from "element-ui";

export default {
  name: "AddShopDialog",
  components: {},
  props: {},
  data() {
    return {
      form: {
        name: "",
        balance: "",
        logo: "",
        sysUsername: "",
        sysPassword: "",
        payPwd: "",
        appPhone: "",
        appPassword: "",
      },
      rules: {
        name: [
          {
            required: true,
            trigger: "blur",
            message: "店名不能为空",
          },
        ],
        balance: [
          {
            required: true,
            message: "余额不能为空",
            trigger: "blur",
          },
        ],
        logo: [
          {
            required: true,
            message: "logo不能为空",
            trigger: "blur",
          },
        ],
        sysUsername: [
          {
            required: true,
            message: "后台用户名不能为空",
            trigger: "blur",
          },
        ],
        sysPassword: [
          {
            required: true,
            message: "后台密码不能为空",
            trigger: "blur",
          },
        ],
        payPwd: [
          {
            required: true,
            message: "支付密码不能为空",
            trigger: "blur",
          },
        ],
        appPhone: [
          {
            required: true,
            message: "A手机号不能为空",
            trigger: "blur",
          },
        ],
        appPassword: [
          {
            required: true,
            message: "密码不能为空",
            trigger: "blur",
          },
        ],
      },
    };
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {},
  methods: {
    onOpen() {},
    onClose() {},
    close() {
      this.resetForm("addShopForm");
      this.$emit("update:visible", false);
    },
    handleConfirm() {
      if (!validPayPwd(this.form.payPwd) || this.form.payPwd.length !== 6) {
        Message({
          type: "warning",
          message: "支付密码只能为6位",
        });
        return;
      }

      this.$refs.addShopForm.validate((valid) => {
        if (!valid) return;
        this.$emit("confirm", { ...this.form });
        this.close();
      });
    },
    uploadPicture(data) {
      this.form.logo = data;
    },
  },
};
</script>

<style scoped lang="scss"></style>
