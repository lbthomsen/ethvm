<template>
  <v-container grid-list-lg class="mb-0">
    <bread-crumbs :newItems="items"></bread-crumbs>
    <v-layout row wrap justify-space-between mb-4>
      <v-flex xs12 md6 lg3>
        <block-last-block></block-last-block>
      </v-flex>
      <v-flex xs12 md6 lg3>
        <block-time-since-last-block></block-time-since-last-block>
      </v-flex>
      <v-flex xs12 md6 lg3>
        <block-hash-rate></block-hash-rate>
      </v-flex>
      <v-flex xs12 md6 lg3>
        <block-difficulty></block-difficulty>
      </v-flex>
    </v-layout>
    <v-layout row wrap justify-center mb-4>
      <v-flex xs12>
        <block-latest-blocks v-if="blocks" :blocks="blocks"></block-latest-blocks>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script lang="ts">
import Visibility from 'visibilityjs'
import Vue from 'vue'
import { Events as sEvents } from 'ethvm-common'
import BN from 'bignumber.js'
const MAX_ITEMS = 20
export default Vue.extend({
  name: 'FrameBlocks',
  data() {
    return {
      blocks: null,
      items: [
        {
          text: this.$i18n.t('title.blocks'),
          disabled: true
        }
      ],
      maxItems: MAX_ITEMS
    }
  },
  created() {
    this.blocks = this.$store.getters.getBlocks
    this.$eventHub.$on(sEvents.newBlock, _block => {
      if (Visibility.state() === 'visible') {
        this.blocks = this.$store.getters.getBlocks
      }
    })
  },
  beforeDestroy() {
    this.$eventHub.$off(sEvents.newBlock)
  },
  computed: {
    getBlocks() {
      return this.blocks.slice(0, this.maxItems)
    }
  }
})
</script>
