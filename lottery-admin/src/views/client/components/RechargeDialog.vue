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
        :model="form"
        ref="rechargeUserForm"
        :rules="rules"
        size="medium"
        label-width="80px"
      >
        <el-form-item label="操作用户">
          <el-input v-model="nickname" :disabled="true"></el-input>
        </el-form-item>

        <el-form-item label="金额" prop="price">
          <el-input v-model.number="form.price" placeholder="请输入金额"></el-input>
        </el-form-item>

        <el-form-item label="操作类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio
              v-for="(item, index) in typeOptions"
              :key="index"
              :label="item.value"
              size="medium"
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
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
  name: "RechargeDialog",
  components: {},
  props: {
    nickname: {
      type: String,
    },
  },
  data() {
    return {
      form: {
        type: "0", // "0"加钱 "1"减钱
        price: undefined,
      },
      rules: {
        price: [
          { required: true, message: "金额不能为空", type: "number", trigger: "blur" },
        ],
      },
      typeOptions: [
        {
          label: "加钱",
          value: "0",
        },
        {
          label: "减钱",
          value: "1",
        },
      ],
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
      this.resetForm("rechargeUserForm");
      this.$emit("update:visible", false);
    },
    handleConfirm() {
      this.$refs.rechargeUserForm.validate((validate) => {
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
