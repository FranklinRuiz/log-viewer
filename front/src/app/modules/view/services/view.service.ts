import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ViewService {

  url = environment.apiurl;

  constructor(private http: HttpClient) { }

  components() {
    return this.http.get<any>(`${this.url}/api/log/component-list`);
  }

  syncLogs(components: any) {
    return this.http.post<any>(`${this.url}/api/log/sync-logs`, components);
  }

  filterLogs(traceId: string, message: string, pageNumber: number) {
    let params = new HttpParams();
    params = params.append('traceId', traceId);
    params = params.append('message', message);
    params = params.append('pageNumber', pageNumber);
    return this.http.get<any>(`${this.url}/api/log/filter`, { params });
  }

}
