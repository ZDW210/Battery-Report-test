import { get } from './request';

export interface ExportResult {
  filename: string;
  data: string;
  mime: string;
}

export function exportEnergyReport(params: { from: string; to: string }) {
  return get<ExportResult>('/export/energy-report', params);
}

export function exportChargingDetails(params: { from: string; to: string }) {
  return get<ExportResult>('/export/charging-details', params);
}
