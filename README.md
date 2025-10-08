# forum-wedkarskie
## Obsługa gita:

#### Zasady:
---
1. Nie używać brancha `main`!
2. Wszystkie feature'y powinny być implementowane na osobnych branchach
3. Gałęzie będą merge'owane przeze mnie (Piotrka). **TY NIC NIE MERGUJESZ**
4. W Jirze na tablicy zadań należy oznaczać nad czym pracujesz. **NIE PRACUJ NAD TYM SAMYM CO INNA OSOBA**
---
#### Zasady nazywania commitów:
1. Wiadomości powinny zaczynać się od słowa kluczowego `FIX:` lub `FEAT:`
2. Wiadomości powinny zawierać nazwę zadania na Jirze, po dwukropku
3. Ewentualne szczegóły zamieścić po znaku `-` po nazwie zadania
##### Przykładowe wiadomości:
- `FEAT: Logowanie`
- `FIX: Logowanie - brak danych w żądaniu HTTP`
---
#### Przed implementacją nowej funkcjonalności należy w kolejności:
1. Pobrać aktualną wersję repozytorium:\
`$ git pull https://github.com/PietREKT/forum-wedkarskie`
2. Utworzyć nowego brancha:  
`$ git branch <nazwa>`\
Nazwy branchy powinny nawiązywać do feature'ów, np:\
`$ git branch login`
3. Przełączyć się na nowego brancha:\
`$ git checkout <nazwa>` 
4. Programować
___
#### Po implementacji nowej funkcjonalności należy w kolejności:
1. Upewnić się, że znajdujemy się na branchu docelowym:\
`$ git branch` 
2. Wykonać commit: \
`$ git commit -m "<wiadomość>"`
3. Wypchnąć zmiany na zdalne repozytorium: \
`$ git push -u origin <branch>`
