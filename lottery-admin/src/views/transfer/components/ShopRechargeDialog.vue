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
        ref="shopRechargeForm"
        :rules="rules"
        size="medium"
        label-width="100px"
      >
        <el-form-item label="充值店铺" prop="name">
          <el-input v-model="form.name" placeholder="请输入店名" readonly=""></el-input>
        </el-form-item>

        <el-form-item label="金额" prop="balance">
          <el-input
            v-model="form.balance"
            placeholder="请输入余额"
            oninput="this.value = this.value.replace(/[^0-9]/g, '');"
          />
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
export default {
  name: "ShopRechargeDialog",
  components: {},
  props: {
    name: {
      type: String,
    },
  },
  data() {
    return {
      form: {
        name: "",
        balance: "",
      },
      rules: {
        balance: [
          {
            required: true,
            message: "充值余额不能为空",
            trigger: "blur",
          },
        ],
      },
    };
  },
  computed: {},
  watch: {
    name(value) {
      this.form.name = value;
    },
  },
  created() {},
  mounted() {},
  methods: {
    onOpen() {},
    onClose() {},
    close() {
      this.resetForm("shopRechargeForm");
      this.$emit("update:visible", false);
    },
    handleConfirm() {
      this.$refs.shopRechargeForm.validate((valid) => {
        if (!valid) return;
        this.$emit("confirm", { ...this.form });
        this.close();
      });
    },
  },
};
</script>

<style scoped lang="scss"></style>
