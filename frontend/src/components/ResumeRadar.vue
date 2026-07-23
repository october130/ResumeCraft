<template>
  <div class="chart-wrapper">
    <v-chart :option="option" autoresize style="width: 100%; height: 300px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import VChart from 'vue-echarts'
import 'echarts'

const props = defineProps({
  scores: { type: Object, default: () => ({}) }
})

const option = computed(() => ({
  radar: {
    indicator: [
      { name: '内容完整性', max: 25 },
      { name: '结构清晰度', max: 20 },
      { name: '技能匹配度', max: 25 },
      { name: '表达专业性', max: 15 },
      { name: '项目经验', max: 15 }
    ],
    shape: 'circle',
    splitArea: { areaStyle: { color: ['rgba(64,158,255,0.02)', 'rgba(64,158,255,0.05)'] } },
    axisLine: { lineStyle: { color: 'rgba(64,158,255,0.2)' } },
    axisLabel: { color: '#606266', fontSize: 12 }
  },
  series: [{
    type: 'radar',
    data: [{
      value: [
        props.scores.contentScore || 0,
        props.scores.structureScore || 0,
        props.scores.skillMatchScore || 0,
        props.scores.expressionScore || 0,
        props.scores.projectScore || 0
      ],
      name: '评分',
      areaStyle: { color: 'rgba(64,158,255,0.2)' },
      lineStyle: { color: '#409eff', width: 2 },
      itemStyle: { color: '#409eff' }
    }]
  }]
}))
</script>

<style scoped>
.chart-wrapper { display: flex; justify-content: center; }
</style>
