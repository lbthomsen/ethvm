import { BalancePayload } from '@app/server/core/payloads'
import { balancePayloadValidator } from '@app/server/core/validation'
import { EthVMServer, SocketEvent, SocketEventValidationResult } from '@app/server/ethvm-server'
import { Events } from 'ethvm-common'

const getBalanceEvent: SocketEvent = {
  id: Events.getBalance,

  onValidate: (server: EthVMServer, socket: SocketIO.Socket, payload: any): SocketEventValidationResult => {
    const valid = balancePayloadValidator(payload) as boolean
    return {
      valid,
      errors: [] // TODO: Map properly the error
    }
  },

  onEvent: (server: EthVMServer, socket: SocketIO.Socket, payload: BalancePayload): Promise<any> => server.vmService.getBalance(payload.address)
}

export default getBalanceEvent
