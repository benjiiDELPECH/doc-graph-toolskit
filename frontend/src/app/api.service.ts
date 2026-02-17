import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Evidence {
  page: number;
  snippet: string;
}

export interface NodeData {
  id: string;
  label: string;
  type: string;
  evidence: Evidence;
}

export interface EdgeData {
  source: string;
  target: string;
  label: string;
  evidence: Evidence;
}

export interface GraphData {
  nodes: NodeData[];
  edges: EdgeData[];
}

export interface Ontology {
  entityTypes: string[];
  relationTypes: string[];
  schema: any;
}

export interface ProcessingResult {
  ontology: Ontology;
  graph: GraphData;
  status: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  processDocument(file: File): Observable<ProcessingResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ProcessingResult>(`${this.apiUrl}/process`, formData);
  }

  getGraph(): Observable<GraphData> {
    return this.http.get<GraphData>(`${this.apiUrl}/graph`);
  }

  checkHealth(): Observable<any> {
    return this.http.get(`${this.apiUrl}/health`);
  }
}
