import { Component } from '@angular/core';
import { DiseaseManagement } from '../../shared/disease-management/disease-management';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DiseaseManagement],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {}
