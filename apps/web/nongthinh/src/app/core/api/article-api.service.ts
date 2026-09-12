import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

export interface ArticleView {
  id: number;
  title: string;
  author: string;
  status: string;
  category: string;
  date: string;
}

@Injectable({ providedIn: 'root' })
export class ArticleApiService {
  list(): Observable<ArticleView[]> {
    return of([]);
  }
}
