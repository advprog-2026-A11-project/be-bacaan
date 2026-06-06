# Profiling dengan Apache JMeter

Folder ini berisi test plan JMeter untuk profiling API `be-bacaan`.

## Prasyarat

- Jalankan aplikasi Spring Boot di port `8082`.
- Install Apache JMeter 5.x dan pastikan command `jmeter` tersedia di `PATH`, atau jalankan dari folder `bin` instalasi JMeter.
- Untuk endpoint authenticated, siapkan JWT Supabase:
  - `student_token` untuk endpoint `/api/student/**`
  - `admin_token` untuk endpoint `/api/admin/**`

## Jalankan aplikasi

```powershell
.\gradlew.bat bootRun
```

Secara default app membaca `.env.local` dan berjalan di `http://localhost:8082`.

## Jalankan JMeter non-GUI

Public baseline tanpa token:

```powershell
jmeter -n -t .\jmeter\be-bacaan-profile.jmx -l .\jmeter\results-public.jtl -e -o .\jmeter\report-public
```

Profiling dengan token dan data bacaan:

```powershell
jmeter -n -t .\jmeter\be-bacaan-profile.jmx `
  -Jstudent_token="PASTE_STUDENT_JWT" `
  -Jadmin_token="PASTE_ADMIN_JWT" `
  -Jreading_id="PASTE_READING_ID" `
  -Jquestion_id="PASTE_QUESTION_ID" `
  -Janswer="A" `
  -Jthreads=25 `
  -Jramp_seconds=30 `
  -Jduration_seconds=180 `
  -l .\jmeter\results.jtl `
  -e -o .\jmeter\report
```

## Parameter penting

| Property | Default                                | Keterangan |
| --- |----------------------------------------| --- |
| `protocol` | `http`                                 | Protocol target |
| `host` | `localhost`                            | Host target |
| `port` | `8082`                                 | Port target |
| `threads` | `10`                                   | Jumlah virtual users |
| `ramp_seconds` | `20`                                   | Waktu menaikkan semua virtual users |
| `duration_seconds` | `120`                                  | Durasi load test |
| `student_token` | kosong                                 | JWT student; jika kosong, flow student dilewati |
| `admin_token` | kosong                                 | JWT admin; jika kosong, flow admin dilewati |
| `reading_id` | `ff45404c-33fe-449e-9251-3cf952703d17` | ID bacaan untuk endpoint detail/quiz |
| `question_id` | `3c8af01b-ee87-43a7-b93d-ed95683d3631` | ID pertanyaan untuk submit quiz |
| `answer` | `B`                                    | Jawaban untuk submit quiz |

![user Defined Variables](Profiling/images/user-defined-variables.png)
## Skenario yang dites

- Public baseline:
  - `GET /actuator/health`
  - `GET /api/student/readings`
  ![Public API Summary Report](Profiling/images/public-api-summary-report.png)
  
- Student flow, hanya berjalan jika `student_token` diisi:
  - `GET /api/student/readings/{readingId}`
  - `GET /api/student/quiz/readings/{readingId}/questions`
  - `POST /api/student/quiz/readings/{readingId}/submit`
  - `GET /api/student/quiz/readings/{readingId}/result`
  - `GET /api/student/readings/stats`
  ![Student Summary Report](Profiling/images/student-summary-report.png)
  
- Admin read-only flow, hanya berjalan jika `admin_token` diisi:
  - `GET /api/admin/readings/reading-list`
  - `GET /api/admin/readings/{readingId}`
  - `GET /api/admin/readings/{readingId}/questions`
  - `GET /api/admin/readings/{readingId}/questions/count`
  ![Admin Summary Report](Profiling/images/admin-summary-report.png)

## Result
![Flame Graph](Profiling/images/flame-graph.png)
![Call Tree](Profiling/images/call-tree.png)
![Timeline](Profiling/images/timeline.png)
Berdasarkan hasil profiling menggunakan JFR, aplikasi **tidak menunjukkan bottleneck signifikan pada CPU maupun memory**.

Masalah utama tidak mengarah ke CPU atau heap, melainkan lebih berpotensi berada pada:

- Akses database
- Query Hibernate/JPA
- Latency network ke PostgreSQL Supabase

### Metrik Utama JFR

| Metrik | Nilai |
| --- | --- |
| Durasi recording | Sekitar 289 detik |
| JVM CPU average | 1.92% |
| JVM CPU max | 20.54% |
| Machine CPU average | 29.50% |
| Heap aktif | Sekitar 50-90 MB |
| Max heap tersedia | Sekitar 3.8 GB |
| Tekanan GC | Tidak terlihat berat |

## Hot Path yang Sering Muncul

Beberapa bagian kode yang sering muncul dalam hasil profiling adalah sebagai berikut.

### `StudentReadingService.java`

Line 28:

```java
readingRepository.findAll();
```

Method ini digunakan untuk mengambil seluruh data bacaan.

### `AdminReadingService.java`

Line 28:

```java
readingRepository.findAll();
```

Method ini juga mengambil seluruh data bacaan dari database.

### `AdminQuizService.java`

Line 84:

```java
validateReadingExists();
quizRepository.findByReadingId(readingId);
```

Pada bagian ini terlihat adanya proses validasi bacaan terlebih dahulu, kemudian dilanjutkan dengan query untuk mengambil daftar pertanyaan berdasarkan `readingId`.

### `QuizService.java`

Line 113:

```java
findByUserIdAndReadingId();
```

Bagian ini digunakan untuk mengambil progress quiz user.

Line 118:

```java
quizRepository.findByReadingId(cleanReadingId);
```

Bagian ini digunakan untuk mengambil daftar pertanyaan berdasarkan bacaan.

## Indikasi Bottleneck

Pada hasil profiling juga terlihat stack dari:

```text
org.postgresql
SSL socket read
```

Hal ini menunjukkan bahwa sebagian request tampaknya sedang menunggu respons dari database remote PostgreSQL Supabase.

Dengan demikian, bottleneck lebih mungkin berasal dari akses database, query Hibernate/JPA, atau latency koneksi ke Supabase, bukan dari komputasi Java di aplikasi.

## Ringkasan Hasil Profiling

Berdasarkan hasil profiling JFR, aplikasi tidak menunjukkan bottleneck signifikan pada CPU maupun memory. Penggunaan CPU JVM rata-rata hanya sekitar 1.92%, sementara heap berada pada kisaran rendah dibandingkan kapasitas maksimum. Hot path yang muncul selama pengujian banyak berada pada operasi repository/JPA seperti `findAll`, `findByReadingId`, dan pencarian progress user.

Hal ini mengindikasikan bahwa potensi bottleneck lebih besar berada pada akses database, query Hibernate, atau latency koneksi ke PostgreSQL Supabase, bukan pada komputasi Java di aplikasi.

## Rekomendasi Optimasi

1. Menghindari penggunaan `findAll()` untuk endpoint list bacaan.

   Gunakan pagination agar aplikasi tidak mengambil seluruh data sekaligus.

2. Menambahkan index database untuk kolom yang sering digunakan dalam query.

   Kolom yang direkomendasikan:

   ```text
   reading_id
   user_id
   user_id + reading_id
   ```

3. Optimalkan endpoint admin questions.

   Hindari query ganda seperti:

   ```java
   existsById();
   findByReadingId();
   ```

   Jika memungkinkan, gabungkan validasi dan query agar akses database lebih efisien.

4. Jalankan profiling ulang setelah aplikasi warm-up.

   Pastikan backend sudah stabil sebelum JMeter dijalankan, supaya hasil profiling tidak tercampur dengan proses startup Spring Boot.
