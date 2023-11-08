<template>
  <div>
    <el-dialog
      v-bind="$attrs"
      v-on="$listeners"
      width="500px"
      @open="onOpen"
      @close="onClose"
    >
      <el-form
        :model="form"
        ref="addSysUserForm"
        :rules="rules"
        size="medium"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码"></el-input>
        </el-form-item>

        <el-form-item label="昵称" prop="name">
          <el-input v-model="form.name" placeholder="请输入昵称"></el-input>
        </el-form-item>

        <el-form-item label="支付密码" prop="payPwd">
          <el-input
            v-model="form.payPwd"
            placeholder="请输入支付密码"
            oninput="this.value = this.value.replace(/[^0-9]/g, '');"
          ></el-input>
        </el-form-item>
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
  name: "AddSysUserDialog",
  components: {},
  props: {},
  data() {
    return {
      form: {
        username: "",
        password: "",
        name: "",
        payPwd: "",
      },
      rules: {
        username: [
          {
            required: true,
            trigger: "blur",
            message: "用户名不能为空",
          },
        ],
        password: [
          {
            required: true,
            message: "密码不能为空",
            trigger: "blur",
          },
        ],
        name: [
          {
            required: true,
            message: "昵称不能为空",
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
      this.resetForm("addSysUserForm");
      this.$emit("update:visible", false);
    },
    handleConfirm() {
      if (!validAlphabets(this.form.username)) {
        Message({
          type: "warning",
          message: "用户名只能是字母",
        });
        return;
      }

      if (!validPayPwd(this.form.payPwd) || this.form.payPwd.length !== 6) {
        Message({
          type: "warning",
          message: "支付密码只能为6位",
        });
        return;
      }

      this.$refs.addSysUserForm.validate((valid) => {
        if (!valid) return;
        this.$emit("confirm", { ...this.form });
        this.close();
      });
    },
  },
};
</script>

<style scoped lang="scss"></style>
