import { Component, ViewChild } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { GraphViewerComponent } from './graph-viewer/graph-viewer.component';
import { ApiService, ProcessingResult } from './api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, HttpClientModule, GraphViewerComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  @ViewChild(GraphViewerComponent) graphViewer?: GraphViewerComponent;
  
  title = 'DocGraph POC';
  selectedFile: File | null = null;
  processing = false;
  result: ProcessingResult | null = null;
  error: string | null = null;

  constructor(private apiService: ApiService) {}

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.selectedFile = file;
      this.error = null;
    } else {
      this.error = 'Please select a PDF file';
      this.selectedFile = null;
    }
  }

  processDocument() {
    if (!this.selectedFile) {
      this.error = 'Please select a file first';
      return;
    }

    this.processing = true;
    this.error = null;
    this.result = null;

    this.apiService.processDocument(this.selectedFile).subscribe({
      next: (result) => {
        this.result = result;
        this.processing = false;
        if (this.graphViewer) {
          this.graphViewer.updateGraph(result.graph);
        }
      },
      error: (err) => {
        this.error = 'Error processing document: ' + (err.error?.message || err.message);
        this.processing = false;
      }
    });
  }

  clearGraph() {
    this.result = null;
    this.selectedFile = null;
    this.error = null;
    if (this.graphViewer) {
      this.graphViewer.clearGraph();
    }
  }
}
