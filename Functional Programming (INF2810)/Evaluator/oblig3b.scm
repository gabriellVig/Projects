(load "evaluator.scm")

;; 1a)

(set! the-global-environment (setup-environment))
(mc-eval '(+ 1 2) the-global-environment)


;; (foo 2 square) returnerer 0 siden vi sjekker om parameteret er 2, og siden det stemmer returnerer vi 0.

;;(foo 4 square) returnerer 16 siden parameteret ikke er 2 utfører vi operasjonen på 4 og ikke på 0 som i forrige eksempel.

;; (cond ((= cond 2) 0) (else (else 4))) returnerer 2 siden cond er definert som 3 og 3 != 2 vil da prosedyren else som vi definerte til (/ x 2) dele tallet vi sender med, 4, på 2. Dermed får vi 2.



;; 2a)

;;La til på de to nederste linjene et lambda uttrykk som legger til og fjerner en fra verdien som sendes med.
(define primitive-procedures
  (list (list 'car car)
        (list 'cdr cdr)
        (list 'cons cons)
        (list 'null? null?)
        (list 'not not)
        (list '+ +)
        (list '- -)
        (list '* *)
        (list '/ /)
        (list '= =)
        (list 'eq? eq?)
        (list 'equal? equal?)
        (list 'display 
              (lambda (x) (display x) 'ok))
        (list 'newline 
              (lambda () (newline) 'ok))
        (list '1- (lambda (x) (- x 1)))
        (list '1+ (lambda (x) (+ x 1)))
;;      her kan vi legge til flere primitiver.
        ))

;;b)
;;Sender med en primitiv med navn og tilhørende kropp. Denne legges til ved bruk at define-variable i den globale omgivelsen
;; Usikker på hvorfor jeg får opp feilmeldingen, men methoden ser ut til å fungere.
(newline)
(define (new-primitive! name body)
  (define-variable! name (list 'primitive body) the-global-environment))

(mc-eval (new-primitive! 'square (lambda (x) (* x x))) the-global-environment)
(mc-eval '(square 3) the-global-environment)

the-global-environment


;;3a)

;; De to første definisjonene er fra evaluatoren, legger bare til or og and på begge.
(define (eval-special-form exp env)
  (cond ((quoted? exp) (text-of-quotation exp))
        ((assignment? exp) (eval-assignment exp env))
        ((definition? exp) (eval-definition exp env))
        ((if? exp) (eval-if exp env))
        ((lambda? exp)
         (make-procedure (lambda-parameters exp)
                         (lambda-body exp)
                         env))
        ((begin? exp) 
         (eval-sequence (begin-actions exp) env))
        ((cond? exp) (mc-eval (cond->if exp) env))
        ((or? exp) (eval-or exp env)) ;; Lagt til
        ((and? exp) (eval-and exp env)))) ;; Lagt til

(define (special-form? exp)
  (cond ((quoted? exp) #t)
        ((assignment? exp) #t)
        ((definition? exp) #t)
        ((if? exp) #t)
        ((lambda? exp) #t)
        ((begin? exp) #t)
        ((cond? exp) #t)
        ((or? exp) #t) ;; Lagt til
        ((and? exp) #t) ;; Lagt til
        (else #f)))


(define (and? exp) (tagged-list? exp 'and))
(define (or? exp) (tagged-list? exp 'or))

(define (eval-and exp env)
  (cond
    ((false? (mc-eval (cadr exp) env)) #f)
    ((null? (cddr exp)) (mc-eval (cadr exp) env))
    (else (eval-and (cons (car exp) (cddr exp)) env))))
      
(define (eval-or exp env)
  (cond
    ((true? (mc-eval (cadr exp) env)) (mc-eval (cadr exp) env))
    ((null? (cddr exp)) #f)
    (else (eval-or (cons (car exp) (cddr exp)) env))))

;; Tester:
(mc-eval '(and (= 1 1)(= 3 5)) the-global-environment)
(mc-eval '(and (= 1 1)(= 2 2)) the-global-environment)

(mc-eval '(or (= 1 4)(= 3 3)) the-global-environment)
(mc-eval '(or (= 5 3)(= 7 5)) the-global-environment)


;; 3b)
;; Fungerer for alle then og elseif, men får ikke else til å fungere slik jeg ønsker.
(define (else? exp) (tagged-list? exp 'else))

(define (eval-if exp env)
  (cond
    ((else? exp) (mc-eval (cadr exp) env))
    ((true? (mc-eval (if-predicate exp) env)) (mc-eval (cadddr exp) env))
    (else (eval-if (cddddr exp) env))))

;; Tester:
(mc-eval '(if (= 2 3)
             'then "test1"
             'elseif (= 4 2)
             'then "test2"
             'elseif (= 3 3)
             'then "test3"
             'else "kommer ikke hit") the-global-environment)

;; 3c)

;; To første definisjonene er fra evaluatoren hvor let er nå lagt til i tilleg til 
(define (special-form? exp)
  (cond ((quoted? exp) #t)
        ((assignment? exp) #t)
        ((definition? exp) #t)
        ((if? exp) #t)
        ((lambda? exp) #t)
        ((begin? exp) #t)
        ((cond? exp) #t)
        ((and? exp) #t) ;; Lagt til
        ((or? exp) #t) ;; Lagt til
        ((let? exp) #t) ;; lagt til for denne oppgaven
        (else #f)))

(define (eval-special-form exp env)
  (cond ((quoted? exp) (text-of-quotation exp))
        ((assignment? exp) (eval-assignment exp env))
        ((definition? exp) (eval-definition exp env))
        ((if? exp) (eval-if exp env))
        ((lambda? exp)
         (make-procedure (lambda-parameters exp)
                         (lambda-body exp)
                         env))
        ((begin? exp) 
         (eval-sequence (begin-actions exp) env))
        ((cond? exp) (mc-eval (cond->if exp) env))
        ((and? exp) (eval-and exp env)) ;; Lagt til
        ((or? exp) (eval-or exp env)) ;; Lagt til
        ((let? exp) (mc-eval (eval-let exp env) env)))) ;; Lagt til for denne oppgaven

(define (make-lambda exp)
  (list 'lambda (map car (cadr exp)) (caddr exp)))

(define (eval-let exp env)
  (append (list (make-lambda exp)) (map cadr (cadr exp))))


;; Tester:
(mc-eval '(let ((a 3)(b 2)(c 3)(d 0))(+ a b c d c)) the-global-environment) ;; 11


;;3d)

(define (eval-let exp env)
  (append (list (make-lambda exp)) (let-exp exp)))

(define (make-lambda exp)
  (append (list 'lambda (let-var exp)) (cdr (member 'in exp))))

(define (let-var exp)
    (cons (cadr exp)
          (if (equal? (car (cddddr exp)) 'and)
              (let-var (cddddr exp))
              '())))

(define (let-exp exp)
    (cons (cadddr exp)
          (if (equal? (car (cddddr exp)) 'and)
              (let-exp (cddddr exp))
              '())))

;; Tester:

(mc-eval '(let x = 2 and
                 y = 3 in
            (display (cons x y)) (newline)
                 (+ x y )) the-global-environment)

;; 3e)
;; Får bare evig løkke
(define (eval-special-form exp env)
  (cond ((quoted? exp) (text-of-quotation exp))
        ((assignment? exp) (eval-assignment exp env))
        ((definition? exp) (eval-definition exp env))
        ((if? exp) (eval-if exp env))
        ((lambda? exp)
         (make-procedure (lambda-parameters exp)
                         (lambda-body exp)
                         env))
        ((begin? exp) 
         (eval-sequence (begin-actions exp) env))
        ((cond? exp) (mc-eval (cond->if exp) env))
        ((and? exp) (eval-and exp env)) ;; Lagt til
        ((or? exp) (eval-or exp env)) ;; Lagt til
        ((let? exp) (mc-eval (eval-let exp env) env)) ;; Lagt til
        ((while? exp) (mc-eval (eval-while exp) env)))) ;; Lagt til for denne oppgaven

(define (special-form? exp)
  (cond ((quoted? exp) #t)
        ((assignment? exp) #t)
        ((definition? exp) #t)
        ((if? exp) #t)
        ((lambda? exp) #t)
        ((begin? exp) #t)
        ((cond? exp) #t)
        ((and? exp) #t) ;; Lagt til
        ((or? exp) #t) ;; Lagt til
        ((let? exp) #t) ;; Lagt til
        ((while? exp) #t) ;; Lagt til for denne oppgaven
        (else #f)))


(define (while? exp) (tagged-list? exp 'while))

(define (eval-while exp)
  ('if (cadr exp) 'then (begin (cddr exp) (eval-while exp))) 'else #f)

;;(mc-eval '(while ((i 1 (+ 1 i)))
   ;; ((= i 4))
 ;; (display i)(newline)) the-global-environment)