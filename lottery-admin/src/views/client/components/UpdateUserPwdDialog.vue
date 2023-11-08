<template>
  <div>
    <el-dialog
      v-bind="$attrs"
      v-on="$listeners"
      width="500px"
      open="onOpen"
      close="onClose"
    >
      <el-form
        ref="updatePwdForm"
        :model="form"
        :rules="rules"
        size="medium"
        label-width="80px"
      >
        <el-row :gutter="0">
          <el-col :span="24">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入密码"></el-input>
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
  name: "UpdateUserPwdDialog",
  components: {},
  props: {},
  data() {
    return {
      form: {
        password: undefined,
      },
      rules: {
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
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
      this.resetForm("updatePwdForm");
      this.$emit("update:visible", false);
    },
    handleConfirm() {
      this.$refs.updatePwdForm.validate((validate) => {
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
