<template>
  <div>
    <el-dialog
      v-bind="$attrs"
      v-on="$listeners"
      width="500px"
      open="onOpen"
      close="onClose"
    >
      <el-form ref="verifyPwdForm" :model="form" size="medium" label-width="80px">
        <el-row :gutter="0">
          <el-col :span="24">
            <el-form-item label="交易密码" prop="payPwd">
              <el-input
                v-model="form.payPwd"
                placeholder="请输入交易密码"
                show-password
              ></el-input>
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
export default {
  name: "VerifyPwdDialog",
  components: {},
  props: {},
  data() {
    return {
      form: {
        payPwd: undefined,
      },
      rules: {
        payPwd: [{ required: true, message: "请输入交易密码", trigger: "blur" }],
      },
    };
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {},
  methods: {
    onOpen() {},
    onclose() {},
    close() {
      this.resetForm("verifyPwdForm");
      this.$emit("update:visible", false);
    },
    handleConfirm() {
      this.$refs.verifyPwdForm.validate((validate) => {
        if (!validate) {
          return;
        }
        this.$emit("confirm", { ...this.form });
        this.close();
      });
    },
  },
};
</script>

<style scoped lang="scss"></style>
