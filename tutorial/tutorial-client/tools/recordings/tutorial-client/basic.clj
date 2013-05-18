{:config {:name :basic, :description "Basic", :order 0}
 :data
 [
  [:node-create [] :map]
  [:node-create [:tutorial] :map]
  [:transform-enable [:tutorial] :increment-counter [{:io.pedestal.app.messages/type :inc, :io.pedestal.app.messages/topic [:my-counter]}]]
  [:node-create [:tutorial :other-counters] :map]
  [:node-create [:tutorial :other-counters "abc"] :map]
  [:value [:tutorial :other-counters "abc"] nil 14]
  [:node-create [:tutorial :other-counters "xyz"] :map]
  [:value [:tutorial :other-counters "xyz"] nil 4]
  [:node-create [:tutorial :avg] :map]
  [:value [:tutorial :avg] nil 9]
  :break
  [:value [:tutorial :other-counters "abc"] 14 15]
  [:value [:tutorial :avg] 9 9.5]
  :break
  [:value [:tutorial :other-counters "xyz"] 4 5]
  [:value [:tutorial :avg] 9.5 10]
  :break
  [:value [:tutorial :other-counters "abc"] 15 16]
  [:value [:tutorial :avg] 10 10.5]
  :break
  [:value [:tutorial :other-counters "xyz"] 5 6]
  [:value [:tutorial :avg] 10.5 11]
  :break
  [:value [:tutorial :other-counters "abc"] 16 17]
  [:value [:tutorial :avg] 11 11.5]
  :break
  [:value [:tutorial :other-counters "xyz"] 6 7]
  [:value [:tutorial :avg] 11.5 12]
  :break
  [:node-create [:tutorial :my-counter] :map]
  [:value [:tutorial :my-counter] nil 1]
  [:value [:tutorial :avg] 12 8.333333333333334]
  :break
  [:value [:tutorial :other-counters "abc"] 17 18]
  [:value [:tutorial :avg] 8.333333333333334 8.666666666666666]
  :break
  [:value [:tutorial :other-counters "xyz"] 7 8]
  [:value [:tutorial :avg] 8.666666666666666 9]
  :break
  [:value [:tutorial :my-counter] 1 2]
  [:value [:tutorial :avg] 9 9.333333333333334]
  :break
  [:value [:tutorial :other-counters "abc"] 18 19]
  [:value [:tutorial :avg] 9.333333333333334 9.666666666666666]
  :break
  [:value [:tutorial :other-counters "xyz"] 8 9]
  [:value [:tutorial :avg] 9.666666666666666 10]
  :break
  [:value [:tutorial :my-counter] 2 3]
  [:value [:tutorial :avg] 10 10.333333333333334]
  :break
  [:value [:tutorial :other-counters "abc"] 19 20]
  [:value [:tutorial :avg] 10.333333333333334 10.666666666666666]
  :break
  [:value [:tutorial :other-counters "xyz"] 9 10]
  [:value [:tutorial :avg] 10.666666666666666 11]
  :break
  [:value [:tutorial :my-counter] 3 4]
  [:value [:tutorial :avg] 11 11.333333333333334]
  :break
  [:value [:tutorial :other-counters "abc"] 20 21]
  [:value [:tutorial :avg] 11.333333333333334 11.666666666666666]
  :break
  [:value [:tutorial :other-counters "xyz"] 10 11]
  [:value [:tutorial :avg] 11.666666666666666 12]
  :break
  [:value [:tutorial :my-counter] 4 5]
  [:value [:tutorial :avg] 12 12.333333333333334]
  :break
  [:value [:tutorial :other-counters "abc"] 21 22]
  [:value [:tutorial :avg] 12.333333333333334 12.666666666666666]
  :break
  [:value [:tutorial :other-counters "xyz"] 11 12]
  [:value [:tutorial :avg] 12.666666666666666 13]
  :break
  [:value [:tutorial :other-counters "abc"] 22 23]
  [:value [:tutorial :avg] 13 13.333333333333334]
  :break
  [:value [:tutorial :other-counters "xyz"] 12 13]
  [:value [:tutorial :avg] 13.333333333333334 13.666666666666666]
 ]}