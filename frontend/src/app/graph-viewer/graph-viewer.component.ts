import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { Network } from 'vis-network/standalone';
import { DataSet } from 'vis-data';
import { GraphData, NodeData, EdgeData } from '../api.service';
import { CommonModule } from '@angular/common';

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
  selectedNode: NodeData | null = null;
  selectedEdge: EdgeData | null = null;

  ngAfterViewInit() {
    this.initNetwork();
  }

  private initNetwork() {
    const container = this.networkContainer.nativeElement;
    const nodes = new DataSet([]);
    const edges = new DataSet([]);

    const data = {
      nodes: nodes,
      edges: edges
    };

    const options = {
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
          type: 'continuous'
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
    this.network.on('selectNode', (params) => {
      if (params.nodes.length > 0) {
        const nodeId = params.nodes[0];
        const node = nodes.get(nodeId);
        if (node) {
          this.selectedNode = {
            id: node.id,
            label: node.label,
            type: node.type,
            evidence: node.evidence
          };
          this.selectedEdge = null;
        }
      }
    });

    this.network.on('selectEdge', (params) => {
      if (params.edges.length > 0) {
        const edgeId = params.edges[0];
        const edge = edges.get(edgeId);
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
    if (!this.network) return;

    const typeColors: { [key: string]: string } = {
      'Person': '#FF6B6B',
      'Organization': '#4ECDC4',
      'Location': '#45B7D1',
      'Date': '#FFA07A',
      'Event': '#98D8C8',
      'Concept': '#95E1D3',
      'Term': '#DDA15E'
    };

    const nodes = graphData.nodes.map(node => ({
      id: node.id,
      label: node.label,
      title: `Type: ${node.type}\nPage: ${node.evidence.page}`,
      color: typeColors[node.type] || '#999999',
      type: node.type,
      evidence: node.evidence
    }));

    const edges = graphData.edges.map((edge, index) => ({
      id: `edge-${index}`,
      from: edge.source,
      to: edge.target,
      label: edge.label,
      title: `Page: ${edge.evidence.page}`,
      evidence: edge.evidence
    }));

    const data = this.network.body.data;
    data.nodes.clear();
    data.edges.clear();
    data.nodes.add(nodes);
    data.edges.add(edges);
  }

  clearGraph() {
    if (this.network) {
      const data = this.network.body.data;
      data.nodes.clear();
      data.edges.clear();
    }
    this.selectedNode = null;
    this.selectedEdge = null;
  }
}
