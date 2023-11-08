<template>
  <div class="statisticsItem">
    <count-to
      :start-val="0"
      :end-val="value"
      :decimals='decimals' 
      :suffix="unit"
      class="normal active"
    ></count-to>
    <span class="normal" style="margin-top: 10px">{{ title }}</span>
  </div>
</template>

<script>
import countTo from "vue-count-to";

export default {
  name: "StatisticsItem",
  components: { countTo },
  props: {
    title: {
      type: String,
      default: "标题",
    },
    value: {
      type: Number,
      default: 0,
    },
    unit: {
      type: String,
      default: "元",
    },
    decimals: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {};
  },
  watch: {},
  mounted() {},
  methods: {},
};
</script>

<style scoped lang="scss">
.normal {
  display: block;
  font-size: 16px;
  text-align: center;
}
.active {
  color: rgb(64, 158, 255);
  font-size: 18px;
  font-weight: bold;
}
</style>
