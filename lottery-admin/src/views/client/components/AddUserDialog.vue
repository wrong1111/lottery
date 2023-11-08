<template>
  <div>
    <el-dialog
      v-bind="$attrs"
      v-on="$listeners"
      width="500px"
      @open="onOpen"
      @close="onClose"
    >
      <el-row :gutter="10">
        <el-form
          :model="form"
          ref="addUserForm"
          :rules="rules"
          size="medium"
          label-width="100px"
        >
          <el-col :span="24">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入手机号"
                maxlength="11"
                oninput="this.value = this.value.replace(/[^0-9]/g, '');"
              ></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>

      <div slot="footer">
        <el-button @click="close">取 消</el-button>
        <el-button type="primary" @click="handleConfirm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { validPhone } from "@/utils/validate";

export default {
  name: "AddUserDialog",
  components: {},
  props: {},
  data() {
    const validatePhone = (rule, value, callback) => {
      if (!validPhone(value)) {
        callback(new Error("请输入正确的手机号"));
      } else {
        callback();
      }
    };

    return {
      form: {
        phone: undefined,
        password: undefined,
      },
      rules: {
        phone: [
          {
            required: true,
            trigger: "blur",
            validator: validatePhone,
          },
        ],
        password: [
          {
            required: true,
            message: "请输入密码",
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
      this.resetForm("addUserForm");
      this.$emit("update:visible", false);
    },
    handleConfirm() {
      this.$refs.addUserForm.validate((valid) => {
        if (!valid) return;
        this.$emit("confirm", { ...this.form });
        this.close();
      });
    },
  },
};
</script>

<style scoped lang="scss"></style>
