import { Fragment } from "react"

type SplitWordsProps = {
  text: string
  mask?: boolean
  className?: string
}

export function SplitWords({ text, mask = true, className }: SplitWordsProps) {
  return (
    <span className={className}>
      {text.split(" ").map((word, i) => (
        <Fragment key={i}>
          {mask ? (
            <span className="-mb-[0.12em] inline-block overflow-hidden pb-[0.12em] align-bottom">
              <span data-word className="inline-block">
                {word}
              </span>
            </span>
          ) : (
            <span data-word className="inline-block">
              {word}
            </span>
          )}{" "}
        </Fragment>
      ))}
    </span>
  )
}
