export const ALARM_LEVEL_MAP: Record<string, { label: string; color: string }> = {
  '1': { label: '一般', color: '#ff9100' },
  '2': { label: '紧急', color: '#ff6d00' },
  '3': { label: '严重', color: '#ff1744' },
};

export const ALARM_CODE_MAP: Record<string, string> = {
  UHIGH1: '过压预警',
  UHIGH2: '过压报警',
  ULOW1: '欠压预警',
  ULOW2: '欠压报警',
  IHIGH1: '过流预警',
  IHIGH2: '过流报警',
  PHIGH1: '过载预警',
  PHIGH2: '过载报警',
  PFLOW1: '功率因数低限预警',
  PFLOW2: '功率因数低限报警',
  LgHIGH1: '漏电超限预警',
  LgHIGH2: '漏电超限报警',
  THIGH1: '超温预警',
  THIGH2: '超温报警',
};

export const PARAM_UNITS: Record<string, string> = {
  Ua: 'V', Ub: 'V', Uc: 'V',
  Ia: 'A', Ib: 'A', Ic: 'A',
  P: 'kW', Pa: 'kW', Pb: 'kW', Pc: 'kW',
  PF: '',
  EPI: 'kWh',
};

export const PARAM_LABELS: Record<string, string> = {
  Ua: 'A相电压', Ub: 'B相电压', Uc: 'C相电压',
  Ia: 'A相电流', Ib: 'B相电流', Ic: 'C相电流',
  P: '总有功功率', Pa: 'A相有功功率', Pb: 'B相有功功率', Pc: 'C相有功功率',
  PF: '功率因数',
  EPI: '正向有功电能',
};
