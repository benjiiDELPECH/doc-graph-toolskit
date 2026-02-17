import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { Network } from 'vis-network/standalone';
import { DataSet } from 'vis-data';
import { GraphData, NodeData, EdgeData } from '../api.service';
import { CommonModule } from '@angular/common';

interface VisNode {
  id: string;
  label: string;
  title?: string;
  color?: string;
  type?: string;
  evidence?: any;
}

interface VisEdge {
  id: string;
  from: string;
  to: string;
  label: string;
  title?: string;
  evidence?: any;
}

@Component({
  selector: 'app-graph-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './graph-viewer.component.html',
  styleUrl: './graph-viewer.component.css'
})
export class GraphViewerComponent implements AfterViewInit {
  @ViewChild('networkContainer', { static: false }) networkContainer!: ElementRef;
  
  private network?: Network;
  private nodesDataSet?: DataSet<VisNode>;
  private edgesDataSet?: DataSet<VisEdge>;
  selectedNode: NodeData | null = null;
  selectedEdge: EdgeData | null = null;

  ngAfterViewInit() {
    this.initNetwork();
  }

  private initNetwork() {
    const container = this.networkContainer.nativeElement;
    this.nodesDataSet = new DataSet<VisNode>([]);
    this.edgesDataSet = new DataSet<VisEdge>([]);

    const data = {
      nodes: this.nodesDataSet,
      edges: this.edgesDataSet
    };

    const options: any = {
      nodes: {
        shape: 'dot',
        size: 16,
        font: {
          size: 14,
          color: '#000000'
        },
        borderWidth: 2,
        borderWidthSelected: 4
      },
      edges: {
        width: 2,
        arrows: {
          to: {
            enabled: true,
            scaleFactor: 0.5
          }
        },
        smooth: {
          enabled: true,
          type: 'continuous',
          roundness: 0.5
        },
        font: {
          size: 12,
          align: 'middle'
        }
      },
      physics: {
        stabilization: true,
        barnesHut: {
          gravitationalConstant: -8000,
          springConstant: 0.04,
          springLength: 95
        }
      },
      interaction: {
        hover: true,
        tooltipDelay: 200
      }
    };

    this.network = new Network(container, data, options);

    // Add event listeners
    this.network.on('selectNode', (params: any) => {
      if (params.nodes.length > 0) {
        const nodeId = params.nodes[0];
        const nodeResult = this.nodesDataSet?.get(nodeId);
        const node = Array.isArray(nodeResult) ? nodeResult[0] : nodeResult;
        if (node) {
          this.selectedNode = {
            id: node.id,
            label: node.label,
            type: node.type || '',
            evidence: node.evidence
          };
          this.selectedEdge = null;
        }
      }
    });

    this.network.on('selectEdge', (params: any) => {
      if (params.edges.length > 0) {
        const edgeId = params.edges[0];
        const edgeResult = this.edgesDataSet?.get(edgeId);
        const edge = Array.isArray(edgeResult) ? edgeResult[0] : edgeResult;
        if (edge) {
          this.selectedEdge = {
            source: edge.from,
            target: edge.to,
            label: edge.label,
            evidence: edge.evidence
          };
          this.selectedNode = null;
        }
      }
    });

    this.network.on('deselectNode', () => {
      this.selectedNode = null;
    });

    this.network.on('deselectEdge', () => {
      this.selectedEdge = null;
    });
  }

  updateGraph(graphData: GraphData) {
    if (!this.network || !this.nodesDataSet || !this.edgesDataSet) return;

    const typeColors: { [key: string]: string } = {
      'Person': '#FF6B6B',
      'Organization': '#4ECDC4',
      'Location': '#45B7D1',
      'Date': '#FFA07A',
      'Event': '#98D8C8',
      'Concept': '#95E1D3',
      'Term': '#DDA15E'
    };

    const nodes: VisNode[] = graphData.nodes.map(node => ({
      id: node.id,
      label: node.label,
      title: `Type: ${node.type}\nPage: ${node.evidence.page}`,
      color: typeColors[node.type] || '#999999',
      type: node.type,
      evidence: node.evidence
    }));

    const edges: VisEdge[] = graphData.edges.map((edge, index) => ({
      id: `edge-${index}`,
      from: edge.source,
      to: edge.target,
      label: edge.label,
      title: `Page: ${edge.evidence.page}`,
      evidence: edge.evidence
    }));

    this.nodesDataSet.clear();
    this.edgesDataSet.clear();
    this.nodesDataSet.add(nodes);
    this.edgesDataSet.add(edges);
  }

  clearGraph() {
    if (this.nodesDataSet && this.edgesDataSet) {
      this.nodesDataSet.clear();
      this.edgesDataSet.clear();
    }
    this.selectedNode = null;
    this.selectedEdge = null;
  }
}
