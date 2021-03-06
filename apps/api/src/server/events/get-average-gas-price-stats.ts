import { StatsPayload } from '@app/server/core/payloads'
import { chartPayloadValidator } from '@app/server/core/validation'
import { EthVMServer, SocketEvent, SocketEventValidationResult } from '@app/server/ethvm-server'
import { Events, Statistic } from 'ethvm-common'

const getAvgGasPriceStats: SocketEvent = {
  id: Events.getAvgGasPriceStats,
  onValidate: (server: EthVMServer, socket: SocketIO.Socket, payload: any): SocketEventValidationResult => {
    const valid = chartPayloadValidator(payload) as boolean
    return {
      valid,
      errors: [] // TODO: Map properly the error
    }
  },

  onEvent: (server: EthVMServer, socket: SocketIO.Socket, payload: StatsPayload): Promise<Statistic[]> =>
    server.statisticsService.getAveragegasPrice(payload.duration)
}

export default getAvgGasPriceStats
