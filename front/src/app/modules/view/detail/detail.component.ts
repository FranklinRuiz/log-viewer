import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss']
})
export class DetailComponent {

  public log: any;

  constructor(
    private sanitizer: DomSanitizer,
    // public dialogRef: MatDialogRef<DetailComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private matSnackBar: MatSnackBar
  ) {
    this.log = data
  }

  formatErrorStack(error: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml('ErrorStack: ' + error.replaceAll('at ', '<br>at&nbsp;'));
  }

  formatMessage(message: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(message.replaceAll(' ', '&nbsp;'));
  }

  copyToClipboard(event: MouseEvent, message: string) {
    event.preventDefault();
    navigator.clipboard.writeText(message).then(() => {
      this.onMessage('Texto copiado');
    }).catch(err => {
      console.error('Error al copiar texto: ', err);
    });
  }

  onMessage(textMessage: string) {
    this.matSnackBar.open(
      textMessage,
      'Cerrar',
      { duration: 3000, verticalPosition: 'bottom', horizontalPosition: 'center' }
    );
  }

}
