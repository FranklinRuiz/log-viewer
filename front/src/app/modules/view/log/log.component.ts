import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DetailComponent } from '../detail/detail.component';
import { FormControl } from '@angular/forms';
import { ViewService } from '../services/view.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { fromEvent } from 'rxjs';
import { distinctUntilChanged, debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-log',
  templateUrl: './log.component.html',
  styleUrls: ['./log.component.scss'],
})
export class LogComponent implements OnInit, OnDestroy, AfterViewInit {

  private subscription!: Subscription;
  private page = 0;
  private type = 'bottom';

  @ViewChild('scrollContainer', { static: false }) private scrollContainer!: ElementRef;
  private scrollSubscription?: Subscription;

  components = new FormControl('');
  componentList: string[] = [];

  displayedColumns = ['timestamp', 'entityName', 'traceId', 'errorMessage', 'message'];
  dataSource: any = [];

  traceId: string = "";
  message: string = "";

  constructor(
    private matSnackBar: MatSnackBar,
    private dialog: MatDialog,
    private service: ViewService
  ) {
  }

  ngOnInit(): void {
    this.onLoadComponents();
    this.onFilterLogs();
  }

  ngAfterViewInit(): void {
    this.scrollSubscription = fromEvent(this.scrollContainer.nativeElement, 'scroll')
      .pipe(
        debounceTime(100),
        distinctUntilChanged()
      )
      .subscribe((e: any) => {
        const scrollTop = e.target.scrollTop;
        const scrollHeight = e.target.scrollHeight;
        const offsetHeight = e.target.offsetHeight;

        if (this.traceId == '') {
          if (scrollTop === 0) {
            this.onScrollUp();
          }

          if (scrollTop + offsetHeight >= scrollHeight) {
            this.onScrollDown();
          }
        }
      });
  }

  scrollToBottom(): void {
    this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight - 950;
  }

  scrollToTop(): void {
    this.scrollContainer.nativeElement.scrollTop = 0 + 100;
  }

  openModal(data: string): void {
    this.dialog.open(DetailComponent, {
      width: '750px',
      height: '100vh',
      data: data,
      position: {
        right: '0px'
      }
    });
  }

  onLoadComponents(): void {
    this.service.components().subscribe({
      next: response => this.componentList = response,
      error: err => console.error(err),
      complete: () => this.onMessage('Carga de componentes completado')
    });
  }

  onSyncLogs(): void {
    let data = (!!this.components.value) ? this.components.value : [];
    this.service.syncLogs(data).subscribe({
      next: response => (!response) ? this.onMessage('Sincronización no se completado') : '',
      error: err => console.error(err),
      complete: () => {
        this.onMessage('Sincronización de logs completado');
        this.page = 0;
        this.onFilterLogs();
      }
    });
  }

  onFilterLogs(): void {
    if (this.traceId != '') {
      this.page = 0;
    }
    this.subscription = this.service.filterLogs(this.traceId, this.message, this.page).subscribe({
      next: response => {
        this.dataSource = response;
      },
      error: err => console.error(err),
      complete: () => {
        setTimeout(() => {
          this.onMessage('Listado de logs completado');
          if (this.type == 'top') {
            this.scrollToTop()
          } else {
            this.scrollToBottom()
          }
        }, 300);
      }
    });
  }

  onScrollUp() {
    this.page++;
    this.type = 'bottom';
    this.onFilterLogs();
  }

  onScrollDown() {
    if (this.page > 0) {
      this.page--;
      this.type = 'top';
      this.onFilterLogs();
    }
  }

  onMessage(textMessage: string) {
    this.matSnackBar.open(
      textMessage,
      'Cerrar',
      { duration: 3000, verticalPosition: 'bottom', horizontalPosition: 'center' }
    );
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
    this.scrollSubscription?.unsubscribe();
  }

}
