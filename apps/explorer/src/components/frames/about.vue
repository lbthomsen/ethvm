<template>
  <v-container grid-list-lg class="mb-0">
    <bread-crumbs :newItems="items"></bread-crumbs>
    <v-layout row wrap justify-start class="mb-4">
      <v-flex xs12>
        <block-about></block-about>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'

const MAX_ITEMS = 20

export default Vue.extend({
  name: 'FramesHome',
  data() {
    return {
      items: [
        {
          text: this.$i18n.t('title.about'),
          disabled: true
        }
      ]
    }
  },
  computed: {
    txs() {
      if (this.$store.getters.getTxs.length) {
        return this.$store.getters.getTxs.slice(0, MAX_ITEMS)
      }
      return []
    }
  }
})
</script>

<style scoped lang="less">
//@import "~lessPath/sunil/frames/about.less";
</style>
